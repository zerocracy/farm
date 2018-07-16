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
package com.zerocracy.farm.guts;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link GsProject}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
public final class GsProjectTest {

    @Test
    public void buildsXml() throws Exception {
        final Project pkt = new GsProject(
            new FkFarm(), "", () -> new Directives().xpath("/guts").add("test")
        );
        try (final Item item = pkt.acq("data.xml")) {
            MatcherAssert.assertThat(
                XhtmlMatchers.xhtml(new TextOf(item.path()).asString()),
                XhtmlMatchers.hasXPaths(
                    "/guts/test",
                    "/guts/jvm/attrs/attr[@id='availableProcessors']"
                )
            );
        }
    }

}
