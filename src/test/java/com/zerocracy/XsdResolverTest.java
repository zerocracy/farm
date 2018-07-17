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
package com.zerocracy;

import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import org.cactoos.io.InputOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Test case for {@link XsdResolver}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class XsdResolverTest {

    @Test
    public void worksWithXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            new StrictXML(
                new XMLDocument(
                    String.join(
                        " ",
                        "<roles updated='2017-07-12T12:00:00' version='1'",
                        "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                        "xsi:noNamespaceSchemaLocation=  ",
                        // @checkstyle LineLengthCheck (1 line)
                        "'http://datum.zerocracy.com/0.27/xsd/pm/staff/roles.xsd'/>"
                    )
                ),
                new XsdResolver()
            ).toString(),
            Matchers.endsWith("/>\n")
        );
    }

    @Test
    public void worksWithXslTransformation() throws Exception {
        final XML before = new XMLDocument(
            String.join(
                " ",
                "<roles updated='2017-07-12T12:00:00' version='2'",
                "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                "xsi:noNamespaceSchemaLocation =",
                "'http://datum.zerocracy.com/0.27/xsd/pm/staff/roles.xsd' />"
            )
        );
        MatcherAssert.assertThat(
            new StrictXML(
                XSLDocument.make(
                    Xocument.class.getResource("compress.xsl")
                ).with("version", "123").transform(before),
                new XsdResolver()
            ).toString(),
            Matchers.endsWith("/>\n")
        );
    }

    @Test
    public void resolvesBasicXsd() throws Exception {
        MatcherAssert.assertThat(
            new XsdResolver().resolveResource(
                "-", "-", "-",
                "http://www.w3.org/2001/XMLSchema-instance#1",
                "-"
            ).getStringData(),
            Matchers.endsWith("</xs:schema>\n")
        );
    }

    @Test
    public void resolvesBasicXsdAsByteStream() throws Exception {
        MatcherAssert.assertThat(
            new TextOf(
                new InputOf(
                    new XsdResolver().resolveResource(
                        "-", "-", "-",
                        // @checkstyle LineLength (1 line)
                        "http://datum.zerocracy.com/0.27/xsd/pm/staff/types.xsd",
                        "-"
                    ).getByteStream()
                )
            ).asString(),
            Matchers.endsWith("</xs:schema>\n")
        );
    }

    @Test
    public void resolvesBasicXsdAsCharStream() throws Exception {
        MatcherAssert.assertThat(
            new TextOf(
                new InputOf(
                    new XsdResolver().resolveResource(
                        "-", "-", "-",
                        // @checkstyle LineLength (1 line)
                        "http://datum.zerocracy.com/0.27/xsd/pm/staff/roles.xsd",
                        "-"
                    ).getCharacterStream()
                )
            ).asString(),
            Matchers.endsWith(":schema>\n")
        );
    }

    @Test
    public void resolvesOnlyOnce() throws Exception {
        final String url = "http://www.w3.org/2001/XMLSchema-instance";
        final LSResourceResolver resolver = new XsdResolver();
        MatcherAssert.assertThat(
            resolver.resolveResource("-", "-", "-", url, "-"),
            Matchers.is(resolver.resolveResource("-", "-", "-", url, "-"))
        );
    }

}
