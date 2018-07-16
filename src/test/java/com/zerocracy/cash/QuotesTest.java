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
package com.zerocracy.cash;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Quotes}.
 * @since 1.0
 */
public final class QuotesTest {

    /**
     * Quotes.DEFAULT can return a quote.
     * @throws Exception If some problem inside
     */
    @Test
    public void returnsQuote() throws Exception {
        MatcherAssert.assertThat(
            Quotes.DEFAULT.quote(Currency.EUR, Currency.EUR),
            Matchers.equalTo(1.0d)
        );
        MatcherAssert.assertThat(
            Quotes.DEFAULT.quote(Currency.USD, Currency.JPY),
            Matchers.greaterThan(1.0d)
        );
    }

}
