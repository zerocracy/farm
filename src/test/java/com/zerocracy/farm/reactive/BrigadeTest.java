/*
 * Copyright (c) 2016-2018 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.MismatchException;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.fake.FkStakeholder;
import com.zerocracy.farm.props.PropsFarm;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.BiFunc;
import org.cactoos.io.InputOf;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Repeated;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Brigade}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class BrigadeTest {

    @Test
    public void parsesGroovy() throws Exception {
        final Path path = Files.createTempDirectory("");
        final Path file = path.resolve("a/b/c/test.groovy");
        file.getParent().toFile().mkdirs();
        new LengthOf(
            new TeeInput(
                String.join(
                    "\n",
                    "import com.zerocracy.Project",
                    "import com.jcabi.xml.XML",
                    "import com.zerocracy.claims.ClaimOut",
                    "import com.zerocracy.Farm",
                    "import com.zerocracy.entry.ClaimsOf",
                    "def exec(Project project, XML xml) {",
                    "Farm farm = binding.variables.farm",
                    "new ClaimOut().type('one more')",
                    ".postTo(new ClaimsOf(farm, project))",
                    "}"
                ),
                file.toFile()
            )
        ).intValue();
        final Project project = new FkProject();
        final Farm farm = new PropsFarm();
        new ClaimOut().type("just some fun")
            .postTo(new ClaimsOf(farm, project));
        final ClaimsItem claims = new ClaimsItem(project).bootstrap();
        final XML xml = claims.iterate().iterator().next();
        final Brigade brigade = new Brigade(
            new StkGroovy(
                new InputOf(file), "brigadetest-parsesgroovy",
                farm
            )
        );
        brigade.apply(project, xml);
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.hasSize(2)
        );
    }

    @Test
    public void runsGroovyScript() throws Exception {
        final Project project = new FkProject();
        new ClaimOut().type("Hello").token("test;notoken").postTo(
            new ClaimsOf(new PropsFarm(), project)
        );
        final ClaimsItem claims = new ClaimsItem(project).bootstrap();
        final XML xml = claims.iterate().iterator().next();
        final Brigade brigade = new Brigade(
            new StkRuntime(
                com.zerocracy.stk.hello.class,
                new PropsFarm()
            )
        );
        brigade.apply(project, xml);
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.hasSize(2)
        );
    }

    @Test
    public void keepsOnlySuccessfulStakeholders() throws Exception {
        final AtomicInteger hits = new AtomicInteger();
        // @checkstyle DiamondOperatorCheck (1 line)
        final Iterable<Stakeholder> pool = new Joined<Stakeholder>(
            new IterableOf<>(
                (pkt, xml) -> {
                    hits.incrementAndGet();
                    throw new MismatchException("oops");
                }
            ),
            new Repeated<>(10, new FkStakeholder())
        );
        final XML claim = new XMLDocument(
            "<claim><type>test</type></claim>"
        ).nodes("/claim").get(0);
        final Project project = new FkProject();
        final BiFunc<Project, XML, Integer> brigade = new Brigade(pool);
        brigade.apply(project, claim);
        brigade.apply(project, claim);
        MatcherAssert.assertThat(hits.get(), Matchers.equalTo(1));
    }

}
