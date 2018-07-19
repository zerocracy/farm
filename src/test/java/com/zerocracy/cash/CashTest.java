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

import java.math.BigDecimal;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.takes.Take;
import org.takes.http.FtRemote;
import org.takes.tk.TkText;

/**
 * Test case for {@link Cash}.
 * @checkstyle MagicNumber (500 lines)
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class CashTest {

    /**
     * Cash can compare two monetary values using Google.
     * @throws Exception If some problem inside
     */
    @Test
    public void comparesMonetaryValuesUsingApi() throws Exception {
        final Take take = new TkText(
            "{\"quotes\": {\"USDEUR\": 1.25}}"
        );
        new FtRemote(take).exec(
            home -> MatcherAssert.assertThat(
                new Cash.S("USD 7")
                    .add(new Cash.S("EUR 11.3"))
                    .quotes(new LoggingQuotes(new ApiLayerQuotes(home)))
                    .compareTo(
                        new Cash.S("JPY 15").add(new Cash.S("GBP 1.5"))
                    ),
                Matchers.greaterThan(0)
            )
        );
    }

    /**
     * Cash can be converted to string and back.
     * @throws Exception If some problem inside
     */
    @Test
    public void convertsItselfToStringAndBack() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("JPY101").precision(1),
            Matchers.hasToString("\u00a5101.0")
        );
        MatcherAssert.assertThat(
            new Cash.S("RUR 101.6"),
            Matchers.hasToString("\u20BD101.60")
        );
        MatcherAssert.assertThat(
            new Cash.S("(GBP 555555.01)"),
            Matchers.hasToString("(\u00a3555555.01)")
        );
        MatcherAssert.assertThat(
            new Cash.S(
                new Cash.S("GBP 90.90").add(new Cash.S("RUR 4.09"))
                    .mul(55433L).div(1000L).toString()
            ),
            Matchers.hasToString("\u00a35038.86 + \u20BD226.72")
        );
    }

    /**
     * Cash can be added to another cash.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsValueToAnotherObject() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("$10").add(new Cash.S("(\u20ac5.76)")),
            Matchers.hasToString("$10.00 + (\u20ac5.76)")
        );
        MatcherAssert.assertThat(
            new Cash.S("$9.0")
                .add(new Cash.S("USD5.2"))
                .add(new Cash.S()),
            Matchers.equalTo(new Cash.S("USD 14.2"))
        );
        MatcherAssert.assertThat(
            new Cash.S("USD 9").add(new Cash.S("(USD 2.13)")),
            Matchers.equalTo(new Cash.S("USD 6.87"))
        );
    }

    /**
     * Cash can be multiplied.
     * @throws Exception If some problem inside
     */
    @Test
    public void multipliesItself() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S(new Cash.S("USD 15").mul(3L).div(100L).toString()),
            Matchers.<Cash>equalTo(new Cash.S("$0.45"))
        );
        MatcherAssert.assertThat(
            new Cash.S("USD 1000")
                .add(new Cash.S("EUR 11"))
                .mul(-25L).div(100L),
            Matchers.hasToString("(€2.75) + ($250.00)")
        );
    }

    /**
     * Cash can be equal to itself.
     * @throws Exception If some problem inside
     */
    @Test
    public void equalsToItself() throws Exception {
        final String money = "(EUR 109.89)";
        MatcherAssert.assertThat(
            new Cash.S(money).hashCode(),
            Matchers.equalTo(new Cash.S(money).hashCode())
        );
        MatcherAssert.assertThat(
            new Cash.S(money),
            Matchers.equalTo(new Cash.S(money))
        );
        MatcherAssert.assertThat(
            new Cash.S(money),
            Matchers.not(Matchers.equalTo(new Cash.S("EUR 45.87")))
        );
    }

    /**
     * Cash can change currencies.
     * @throws Exception If some problem inside
     */
    @Test
    public void exchangesUsingQuotes() throws Exception {
        final Quotes quotes = Mockito.mock(Quotes.class);
        Mockito.doReturn(1.25d).when(quotes).quote(Currency.EUR, Currency.USD);
        MatcherAssert.assertThat(
            new Cash.S("USD 5")
                .add(new Cash.S("EUR 10"))
                .quotes(quotes)
                .exchange(Currency.USD),
            Matchers.hasToString("$17.50")
        );
    }

    /**
     * Cash can compress similar currencies.
     * @throws Exception If some problem inside
     */
    @Test
    public void compressesSimilarCurrencies() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("USD 57")
                .add(new Cash.S("(EUR 6)"))
                .add(new Cash.S("USD 56")),
            Matchers.hasToString(Matchers.containsString("$113.00"))
        );
    }

    /**
     * Cash can compress zero pairs.
     * @throws Exception If some problem inside
     */
    @Test
    public void compressesZeroPairs() throws Exception {
        final String txt = "USD 32";
        MatcherAssert.assertThat(
            new Cash.S(txt)
                .add(new Cash.S("EUR 0.0"))
                .add(new Cash.S("JPY 0")),
            Matchers.hasToString("$32.00")
        );
        MatcherAssert.assertThat(
            new Cash.S(txt).add(new Cash.S("EUR 99.9")).mul(0L),
            Matchers.equalTo(new Cash.S())
        );
        MatcherAssert.assertThat(
            new Cash.S("GBP 0.00"),
            Matchers.hasToString("0")
        );
    }

    /**
     * Cash can detect when it's empty.
     * @throws Exception If some problem inside
     */
    @Test
    public void detectsEmptyState() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S().isEmpty(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Cash.S("EUR 0"),
            Matchers.equalTo(new Cash.S())
        );
        MatcherAssert.assertThat(
            new Cash.S("USD 3").isEmpty(),
            Matchers.is(false)
        );
    }

    /**
     * Cash can compare differently ordered pairs.
     * @throws Exception If some problem inside
     */
    @Test
    public void comparesDifferentlyOrderedPairs() throws Exception {
        final String first = "USD 37";
        final String second = "RUR 9.50";
        MatcherAssert.assertThat(
            new Cash.S(first).add(new Cash.S(second)),
            Matchers.equalTo(
                new Cash.S(second).add(new Cash.S(first))
            )
        );
    }

    /**
     * Cash can change precision.
     * @throws Exception If some problem inside
     */
    @Test
    public void alternatesPrecision() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("USD 50.35").precision(1),
            Matchers.hasToString("$50.4")
        );
        MatcherAssert.assertThat(
            new Cash.S("USD 44").precision(4),
            Matchers.hasToString("$44.0000")
        );
    }

    /**
     * Cash can handle zero value gracefully.
     * @throws Exception If some problem inside
     */
    @Test
    public void handlesZeroValueGracefully() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S(new Cash.S().toString()).isEmpty(),
            Matchers.is(true)
        );
    }

    /**
     * Cash can be serialized.
     * @throws Exception If some problem inside
     */
    @Test
    public void serializesItselfAndBack() throws Exception {
        final Cash cash = new Cash.S("USD 88.4").precision(3);
        final byte[] data = SerializationUtils.serialize(cash);
        MatcherAssert.assertThat(
            Cash.class.cast(SerializationUtils.deserialize(data)),
            Matchers.hasToString("$88.400")
        );
    }

    /**
     * Cash can convert to BigDecimal.
     * @throws Exception If some problem inside
     */
    @Test
    public void convertsToBigDecimal() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("EUR 57.43009").decimal(),
            Matchers.equalTo(new BigDecimal("57.43"))
        );
    }

    /**
     * Cash can catch invalid formatting.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void catchesBrokenFormatting() throws Exception {
        final String[] mistakes = {
            "", "10$", "500", "$  90",
        };
        for (final String mistake : mistakes) {
            try {
                new Cash.S(mistake);
            } catch (final CashParsingException ex) {
                MatcherAssert.assertThat(
                    ex.getLocalizedMessage(),
                    Matchers.containsString(mistake)
                );
            }
        }
    }

    /**
     * Cash can divide by cash.
     * @throws Exception If some problem inside
     */
    @Test
    public void dividesCashByCash() throws Exception {
        MatcherAssert.assertThat(
            new Cash.S("USD 90").div(new Cash.S("$180")),
            Matchers.equalTo(0.5d)
        );
    }

}
