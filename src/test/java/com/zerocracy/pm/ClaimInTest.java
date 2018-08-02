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
package com.zerocracy.pm;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.claims.ClaimIn;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Test case for {@link ClaimIn}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ClaimInTest {

    @Test
    public void readsParts() throws Exception {
        final ClaimIn claim = new ClaimIn(
            ClaimInTest.claim(
                "<claim id='1'><author>yegor256</author></claim>"
            )
        );
        MatcherAssert.assertThat(
            claim.author(),
            Matchers.equalTo("yegor256")
        );
    }

    @Test
    public void buildsClaimOut() throws Exception {
        final ClaimIn claim = new ClaimIn(
            ClaimInTest.claim(
                String.join(
                    "",
                    "<claim id='1'><type>ZZ</type>",
                    "<author>jeff</author><token>X</token></claim>"
                )
            )
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(
                    new Directives().add("claims").append(
                        claim.reply("Hello")
                    )
                ).xmlQuietly()
            ),
            XhtmlMatchers.hasXPaths(
                "/claims/claim[type='Notify']",
                "/claims/claim[token='X']",
                "/claims/claim/params/param[@name='cause']",
                "/claims/claim/params/param[@name='message' and .='Hello']"
            )
        );
    }

    @Test
    public void makesCopy() throws Exception {
        final ClaimIn claim = new ClaimIn(
            ClaimInTest.claim(
                String.join(
                    "",
                    "<claim id='1'><author>yegor</author> ",
                    "<type>the type</type> ",
                    "<token>the-token</token> ",
                    "<params><param name='a'>hello</param></params></claim>"
                )
            )
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(
                    new Directives().add("zz").append(
                        claim.copy()
                    )
                ).xmlQuietly()
            ),
            XhtmlMatchers.hasXPaths(
                "/zz/claim[type='the type']",
                "/zz/claim[not(author)]",
                "/zz/claim/params/param[@name='a' and .='hello']"
            )
        );
    }

    @Test
    public void errorClaim() throws Exception {
        MatcherAssert.assertThat(
            new ClaimIn(
                ClaimInTest.claim("<claim id='1'><type>Error</type></claim>")
            ).isError(),
            Matchers.is(true)
        );
    }

    /**
     * Claim from text.
     * @param xml XML text
     * @return Claim
     */
    private static XML claim(final String xml) {
        return new XMLDocument(xml).nodes("/claim").get(0);
    }
}
