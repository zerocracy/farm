/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.zold;

import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link Zold}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ZoldITCase {

    @BeforeClass
    public static void setUp() throws Exception {
        Assume.assumeTrue(
            "Zold integration test is not configured",
            new Props().has("//zold/secret")
        );
    }

    @Test
    public void fetchRateFromWts() throws Exception {
        MatcherAssert.assertThat(
            new Zold(new PropsFarm()).rate().compareTo(BigDecimal.ONE),
            Matchers.equalTo(1)
        );
    }

    @Test
    public void payZold() throws Exception {
        MatcherAssert.assertThat(
            new Zold(new PropsFarm()).pay(
                "yegor256",
                new Cash.S("$0.10").decimal(),
                "ZoldITCase#payZold"
            ),
            Matchers.not(Matchers.isEmptyString())
        );
    }

    @Test
    public void readWalletId() throws Exception {
        MatcherAssert.assertThat(
            new Zold(new PropsFarm()).wallet(),
            Matchers.not(Matchers.isEmptyString())
        );
    }

    @Test
    public void createsInvoice() throws Exception {
        final ZldInvoice invoice = new Zold(new PropsFarm()).invoice();
        MatcherAssert.assertThat(
            "prefix", invoice.prefix(),
            Matchers.not(Matchers.isEmptyString())
        );
        MatcherAssert.assertThat(
            "invoice", invoice.invoice(),
            Matchers.not(Matchers.isEmptyString())
        );
    }
}
