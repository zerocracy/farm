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

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.farm.MismatchException;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Pmo;
import org.cactoos.io.InputOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link StkGroovy}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class StkGroovyTest {

    @Test
    public void parsesGroovy() throws Exception {
        final Farm farm = new PropsFarm();
        final Project project = new Pmo(farm);
        new StkGroovy(
            new InputOf(
                String.join(
                    "\n",
                    "import com.zerocracy.Project",
                    "import com.jcabi.xml.XML",
                    "import com.zerocracy.claims.ClaimOut",
                    "import com.zerocracy.farm.props.Props",
                    "import com.zerocracy.Farm",
                    "import com.zerocracy.entry.ClaimsOf",
                    "def exec(Project project, XML xml) {",
                    "Farm farm = binding.variables.farm",
                    "new ClaimOut()",
                    "  .type(new Props(project).get('//testing'))",
                    "  .postTo(new ClaimsOf(farm, project))",
                    "}"
                )
            ),
            "stkgroovytest-parsesgroovy",
            farm
        ).process(project, null);
        final ClaimsItem claims = new ClaimsItem(project).bootstrap();
        MatcherAssert.assertThat(
            new ClaimIn(claims.iterate().iterator().next()).type(),
            Matchers.equalTo("yes")
        );
    }

    @Test(expected = SoftException.class)
    public void letsSoftExceptionFloatUp() throws Exception {
        final Project project = new FkProject();
        new StkGroovy(
            new InputOf(
                String.join(
                    "\n",
                    "import com.zerocracy.Project ",
                    "import com.zerocracy.SoftException",
                    "import com.jcabi.xml.XML ",
                    "def exec(Project project, XML xml) { ",
                    "throw new SoftException('intended')",
                    "} "
                )
            ),
            "stkgroovytest-floats-soft",
            new FkFarm()
        ).process(project, null);
    }

    @Test(expected = MismatchException.class)
    public void letsMismatchExceptionFloatUp() throws Exception {
        final Project project = new FkProject();
        new StkGroovy(
            new InputOf(
                String.join(
                    "\n",
                    "import com.zerocracy.Project  ",
                    "import com.zerocracy.farm.MismatchException",
                    "import com.jcabi.xml.XML  ",
                    "def exec(Project project, XML xml) {  ",
                    "throw new MismatchException('intended')",
                    "}  "
                )
            ),
            "stkgroovytest-floats-mismatch",
            new FkFarm()
        ).process(project, null);
    }

    @Test
    public void rethrowsCorrectly() throws Exception {
        try {
            new StkGroovy(
                new InputOf(
                    String.join(
                        "\n",
                        "import com.zerocracy.Project   ",
                        "import com.jcabi.xml.XML   ",
                        "def exec(Project project, XML xml) {   ",
                        "throw new IllegalStateException('boom!')",
                        "}   "
                    )
                ),
                "stkgroovytest-runtime-exception",
                new FkFarm()
            ).process(new FkProject(), null);
        } catch (final IllegalStateException ex) {
            MatcherAssert.assertThat(
                new TextOf(ex).asString(),
                Matchers.containsString("boom!")
            );
        }
    }
}
