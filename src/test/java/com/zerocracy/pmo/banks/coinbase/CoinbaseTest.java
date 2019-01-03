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
package com.zerocracy.pmo.banks.coinbase;

import java.io.IOException;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link Coinbase}.
 *
 * @since 0.21.1
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class CoinbaseTest {

    /**
     * Initialize {@link Coinbase} instance to run tests.
     */
    private Coinbase cbs;

    @Before
    public void setUp() {
        Assume.assumeNotNull(this.cbs);
    }

    @Test
    public void testSCopes() throws IOException {
        MatcherAssert.assertThat(
            this.cbs.scopes(),
            Matchers.containsInAnyOrder(
                "wallet:accounts:read",
                "wallet:buys:create",
                "wallet:buys:read",
                "wallet:transactions:read",
                "wallet:transactions:send"
            )
        );
    }

    @Test
    public void testFetchBalance() throws IOException {
        MatcherAssert.assertThat(
            this.cbs.balance().compareTo(BigDecimal.ZERO),
            new IsEqual<>(0)
        );
    }

    @Test
    public void transactionsTest() throws IOException {
        MatcherAssert.assertThat(
            this.cbs.transactions(),
            Matchers.emptyIterable()
        );
    }

    @Test
    public void sendTest() throws IOException {
        this.cbs.send(
            "USD", "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2",
            new BigDecimal("0.01"),
            "test", "idem"
        );
    }

    @Test
    public void buyTest() throws IOException {
        this.cbs.buy(
            "BTC",
            BigDecimal.ONE,
            false
        );
    }
}
