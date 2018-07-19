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
package com.zerocracy.pm.cost;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkProject;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link Equity}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class EquityTest {

    @Test
    public void addValue() throws Exception {
        final Project pkt = new FkProject();
        final Equity equity = new Equity(pkt).bootstrap();
        try (final Item item = pkt.acq("equity.xml")) {
            new LengthOf(
                new TeeInput(
                    String.join(
                        " ",
                        "<equity",
                        "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                        // @checkstyle LineLength (1 line)
                        "xsi:noNamespaceSchemaLocation='http://datum.zerocracy.com/0.52/xsd/pm/cost/equity.xsd'",
                        "version='0.1' updated='2017-03-27T11:18:09.228Z'>",
                        "<cap>$400000</cap>",
                        "<shares>50000</shares>",
                        "<owners><owner id='yegor256'>100</owner></owners>",
                        "</equity>"
                    ),
                    item.path()
                )
            ).intValue();
        }
        final String login = "yegor256";
        equity.add(login, new Cash.S("$500"));
        try (final Item item = pkt.acq("equity.xml")) {
            MatcherAssert.assertThat(
                XhtmlMatchers.xhtml(new TextOf(item.path()).asString()),
                XhtmlMatchers.hasXPath(
                    "/equity/owners/owner[@id='yegor256' and .= '162.5']"
                )
            );
            MatcherAssert.assertThat(
                equity.ownership(login),
                Matchers.equalTo(
                    "162.50 shares = $1296.00/0.325000% of $400000.00"
                )
            );
        }
    }

    @Test
    public void worksWithEmptyEquity() throws Exception {
        final Equity equity = new Equity(new FkProject()).bootstrap();
        MatcherAssert.assertThat(
            equity.ownership("jeff"),
            Matchers.equalTo("")
        );
    }

    @Test
    @Ignore
    public void createsPdf() throws Exception {
        final Project pkt = new FkProject();
        final Equity equity = new Equity(pkt).bootstrap();
        try (final Item item = pkt.acq("equity.xml")) {
            new LengthOf(
                new TeeInput(
                    String.join(
                        " ",
                        "<equity",
                        "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                        // @checkstyle LineLength (1 line)
                        "xsi:noNamespaceSchemaLocation='http://datum.zerocracy.com/0.52/xsd/pm/cost/equity.xsd'",
                        "version='0.1' updated='2017-03-27T11:18:09.228Z'>",
                        "<cap>$300000</cap>",
                        "<shares>70000</shares>",
                        "<owners><owner id='dmarkov'>500</owner></owners>",
                        "</equity>"
                    ),
                    item.path()
                )
            ).intValue();
        }
        MatcherAssert.assertThat(
            equity.pdf("dmarkov"),
            Matchers.notNullValue()
        );
    }

}
