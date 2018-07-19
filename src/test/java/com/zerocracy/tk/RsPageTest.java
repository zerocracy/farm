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
package com.zerocracy.tk;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.cactoos.io.InputOf;
import org.cactoos.list.ListOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.rq.RqFake;

/**
 * Test case for {@link RsPage}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RsPageTest {

    @Test
    public void rendersHtml() throws IOException {
        MatcherAssert.assertThat(
            new TextOf(
                new InputOf(
                    new InputStreamReader(
                        new RsPage(
                            new PropsFarm(new FkFarm()),
                            "/xsl/spam.xsl",
                            new RqFake(
                                new ListOf<>(
                                    "GET /board",
                                    "Host: www.example.com",
                                    // @checkstyle LineLength (2 lines)
                                    "Accept: text/html,application/xhtml+xml,application/xml",
                                    "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:62.0) Gecko/20100101 Firefox/62.0"
                                ),
                                ""
                            ),
                            Collections.emptyList()
                        ).body(),
                        StandardCharsets.UTF_8
                    )
                )
            ).asString(),
            XhtmlMatchers.hasXPath("//xhtml:body")
        );
    }
}
