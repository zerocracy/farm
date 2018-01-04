/**
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
package com.zerocracy;

import com.jcabi.matchers.XhtmlMatchers;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.text.JoinedText;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Xocument}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class XocumentTest {

    @Test
    public void upgradesXmlDocument() throws Exception {
        final Path temp = Files.createTempFile("xocument", ".xml");
        new LengthOf(
            new TeeInput(
                new JoinedText(
                    " ",
                    "<catalog version='0.32.2'",
                    "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                    "xsi:noNamespaceSchemaLocation=",
                    "'http://datum.zerocracy.com/0.32.3/xsd/pmo/catalog.xsd'>",
                    "<project id='ABCDEFGHT'>",
                    "<fee>$5</fee>",
                    "<created>2017-01-02T12:00:00</created></project>",
                    "<project id='ABCDEFGHI'>",
                    "<created>2017-01-01T12:00:00</created></project>",
                    "</catalog>"
                ),
                temp
            )
        ).intValue();
        MatcherAssert.assertThat(
            new Xocument(temp)
                .bootstrap("pmo/catalog")
                .nodes("/catalog/project"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            new TextOf(temp).asString(),
            XhtmlMatchers.hasXPath("/catalog/project/publish")
        );
    }

}
