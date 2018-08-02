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
package com.zerocracy.claims;

import com.jcabi.xml.XML;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ClaimSignature}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ClaimSignatureTest {
    @Test
    public void computesHash() throws Exception {
        MatcherAssert.assertThat(
            new ClaimSignature(
                new ClaimToXml(
                    new ClaimOut()
                        .type("signature-test")
                        .author("unit-test")
                        .token("telegram:1234")
                        .param("one", "111")
                        .param("two", "22222")
                        .param("three", "33")
                ).asXml()
            ).asString(),
            Matchers.equalTo(
                "9cXy+OUVpYzSGISwb9zoCXAlAeikgBzCQegyWXpumOg="
            )
        );
    }

    @Test
    public void withDifferentType() throws Exception {
        MatcherAssert.assertThat(
            new ClaimSignature(
                new ClaimToXml(
                    new ClaimOut()
                        .type("type-one")
                ).asXml()
            ).asString().equals(
                new ClaimSignature(
                    new ClaimToXml(
                        new ClaimOut()
                            .type("type-two")
                    ).asXml()
                ).asString()
            ),
            Matchers.is(false)
        );
    }

    @Test
    public void withDifferentAuthors() throws Exception {
        MatcherAssert.assertThat(
            new ClaimSignature(
                new ClaimToXml(
                    new ClaimOut()
                        .type("test")
                        .author("author-one")
                ).asXml()
            ).asString().equals(
                new ClaimSignature(
                    new ClaimToXml(
                        new ClaimOut()
                            .type("test")
                            .author("author-two")
                    ).asXml()
                ).asString()
            ),
            Matchers.is(false)
        );
    }

    @Test
    public void withDifferentTokens() throws Exception {
        MatcherAssert.assertThat(
            new ClaimSignature(
                new ClaimToXml(
                    new ClaimOut()
                        .type("test")
                        .token("token-one")
                ).asXml()
            ).asString().equals(
                new ClaimSignature(
                    new ClaimToXml(
                        new ClaimOut()
                            .type("test")
                            .token("token-two")
                    ).asXml()
                ).asString()
            ),
            Matchers.is(false)
        );
    }

    @Test
    public void withDifferentParams() throws Exception {
        MatcherAssert.assertThat(
            new ClaimSignature(
                new ClaimToXml(
                    new ClaimOut()
                        .type("test")
                        .param("name", "value1")
                ).asXml()
            ).asString().equals(
                new ClaimSignature(
                    new ClaimToXml(
                        new ClaimOut()
                            .type("test")
                            .param("name", "value2")
                    ).asXml()
                ).asString()
            ),
            Matchers.is(false)
        );
    }

    /**
     * Convert claim to xml.
     */
    private static final class ClaimToXml {

        /**
         * Source claim.
         */
        private final ClaimOut src;

        /**
         * Ctor.
         *
         * @param claim Source claim
         */
        ClaimToXml(final ClaimOut claim) {
            this.src = claim;
        }

        /**
         * Convert to xml.
         *
         * @return XML
         * @throws IOException If fails
         */
        public XML asXml() throws IOException {
            final AtomicReference<XML> xml = new AtomicReference<>();
            this.src.postTo(
                claim -> xml.set(claim.nodes("//claim").get(0))
            );
            return xml.get();
        }
    }
}
