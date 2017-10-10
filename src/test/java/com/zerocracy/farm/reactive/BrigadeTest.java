/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.farm.fake.FkProject;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.InputOf;
import org.cactoos.io.LengthOf;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Brigade}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
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
                    "import com.zerocracy.jstk.Project",
                    "import com.jcabi.xml.XML",
                    "import com.zerocracy.pm.ClaimOut",
                    "def exec(Project project, XML xml) {",
                    "new ClaimOut().type('one more').postTo(project)",
                    "}"
                ),
                file.toFile()
            )
        ).value();
        final Project project = new FkProject();
        new ClaimOut().type("just some fun").postTo(project);
        final Claims claims = new Claims(project).bootstrap();
        final XML xml = claims.iterate().iterator().next();
        final Brigade brigade = new Brigade(
            new StkGroovy(new InputOf(file), "brigadetest-parsesgroovy")
        );
        brigade.process(project, xml);
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.hasSize(2)
        );
    }

    @Test
    public void parsesGroovyScript() throws Exception {
        final Project project = new FkProject();
        new ClaimOut().type("hello").token("test;notoken").postTo(project);
        final Claims claims = new Claims(project).bootstrap();
        final XML xml = claims.iterate().iterator().next();
        final Brigade brigade = new Brigade(
            new StkGroovy(
                new ResourceOf("com/zerocracy/stk/hello.groovy"),
                "brigadetest-parsesgroovyscript"
            )
        );
        brigade.process(project, xml);
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.hasSize(2)
        );
    }

}
