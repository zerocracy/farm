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
package com.zerocracy.cash;

import java.io.IOException;
import java.net.URL;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration case for {@link Cash}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.6
 */
public final class CashITCase {

    /**
     * Assume we're online.
     */
    @Before
    public void weAreOnline() {
        try {
            new URL("http://www.google.com").getContent();
        } catch (final IOException ex) {
            Assume.assumeTrue(false);
        }
    }

    /**
     * Cash can compare two monetary values using Google.
     * @throws Exception If some problem inside
     */
    @Test
    public void comparesMonetaryValuesUsingGoogleQuotes() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("USD 7")
                .add(new Cash.S("EUR 11"))
                .quotes(new GerQuotes())
                .compareTo(
                    new Cash.S("JPY 15").add(new Cash.S("GBP 1.5"))
                ),
            Matchers.greaterThan(0)
        );
    }

}
