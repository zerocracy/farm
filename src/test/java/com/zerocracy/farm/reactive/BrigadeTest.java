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
import com.zerocracy.jstk.fake.FkProject;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.BytesAsInput;
import org.cactoos.io.FileAsOutput;
import org.cactoos.io.LengthOfInput;
import org.cactoos.io.PathAsInput;
import org.cactoos.io.ResourceAsInput;
import org.cactoos.io.TeeInput;
import org.cactoos.text.TextAsBytes;
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
        new LengthOfInput(
            new TeeInput(
                new BytesAsInput(
                    new TextAsBytes(
                        String.join(
                            "\n",
                            "import com.zerocracy.pm.ClaimOut",
                            "import com.jcabi.xml.XML",
                            "import com.zerocracy.jstk.Project",
                            "def exec(Project project, XML xml) {",
                            "new ClaimOut().type('one more').postTo(project)",
                            "}"
                        )
                    )
                ),
                new FileAsOutput(file.toFile())
            )
        ).value();
        final Project project = new FkProject();
        new ClaimOut().type("just some fun").postTo(project);
        final XML xml;
        try (final Claims claims = new Claims(project).lock()) {
            xml = claims.iterate().iterator().next();
        }
        final Brigade brigade = new Brigade(
            new StkGroovy(new PathAsInput(file), "brigadetest-parsesgroovy")
        );
        brigade.process(project, xml);
        try (final Claims claims = new Claims(project).lock()) {
            MatcherAssert.assertThat(
                claims.iterate(),
                Matchers.hasSize(2)
            );
        }
    }

    @Test
    public void parsesGroovyScript() throws Exception {
        final Project project = new FkProject();
        new ClaimOut().type("hello").token("notoken").postTo(project);
        final XML xml;
        try (final Claims claims = new Claims(project).lock()) {
            xml = claims.iterate().iterator().next();
        }
        final Brigade brigade = new Brigade(
            new StkGroovy(
                new ResourceAsInput("com/zerocracy/stk/hello.groovy"),
                "brigadetest-parsesgroovyscript"
            )
        );
        brigade.process(project, xml);
        try (final Claims claims = new Claims(project).lock()) {
            MatcherAssert.assertThat(
                claims.iterate(),
                Matchers.hasSize(2)
            );
        }
    }

}
