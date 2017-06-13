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
package com.zerocracy.pmo;

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.fake.FkProject;
import org.cactoos.io.BytesAsInput;
import org.cactoos.io.LengthOfInput;
import org.cactoos.io.PathAsOutput;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Ext}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ExtTest {

    @Test
    public void fetchesProps() throws Exception {
        final Project project = new FkProject();
        try (final Item item = project.acq("ext.xml")) {
            new LengthOfInput(
                new TeeInput(
                    new BytesAsInput(
                        String.join(
                            "",
                            // @checkstyle LineLength (2 lines)
                            "<ext xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                            " xsi:noNamespaceSchemaLocation='https://raw.githubusercontent.com/zerocracy/datum/0.23/xsd/pmo/ext.xsd'>",
                            "<system id='github'>",
                            "<prop id='login'>yegor256</prop>",
                            "</system></ext>"
                        )
                    ),
                    new PathAsOutput(item.path())
                )
            ).asValue();
        }
        MatcherAssert.assertThat(
            new Ext(project).bootstrap().get("github", "login"),
            Matchers.equalTo("yegor256")
        );
    }

    @Test
    public void setsProps() throws Exception {
        final String system = "slack";
        final String prop = "username";
        final String value = "jeff";
        MatcherAssert.assertThat(
            new Ext(new FkProject())
                .bootstrap()
                .set(system, prop, value)
                .get(system, prop),
            Matchers.equalTo(value)
        );
    }

}
