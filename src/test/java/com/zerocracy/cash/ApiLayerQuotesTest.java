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
import org.takes.Take;
import org.takes.http.FtRemote;
import org.takes.tk.TkText;

/**
 * Integration case for {@link ApiLayerQuotes}.
 * @since 1.0
 */
public final class ApiLayerQuotesTest {

    /**
     * If there are no USD the quote() throws an exception.
     * @throws Exception If some problem inside
     */
    @Test(expected = IllegalArgumentException.class)
    public void noUsdIsNotAccepted() throws Exception {
        new ApiLayerQuotes().quote(Currency.EUR, Currency.GBP);
    }

    /**
     * Works with JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void returnsQuote() throws Exception {
        final Take take = new TkText(
            "{\"quotes\": {\"USDEUR\": 1.25}}"
        );
        new FtRemote(take).exec(
            home -> MatcherAssert.assertThat(
                new LoggingQuotes(new ApiLayerQuotes(home))
                    .quote(Currency.EUR, Currency.USD),
                // @checkstyle MagicNumber (1 lines)
                Matchers.equalTo(0.8d)
            )
        );
    }

}
