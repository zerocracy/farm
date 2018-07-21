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

/**
 * Test case for {@link Pair}.
 * @since 1.0
 * @checkstyle MagicNumber (500 lines)
 */
public final class PairTest {

    /**
     * Pair can be converted to string and back.
     * @throws Exception If some problem inside
     */
    @Test
    public void convertsItselfToStringAndBack() throws Exception {
        MatcherAssert.assertThat(
            Pair.valueOf("JPY 101"),
            Matchers.hasToString(Matchers.endsWith("\u00a5101.00"))
        );
        MatcherAssert.assertThat(
            Pair.valueOf("RUR 101.6"),
            Matchers.hasToString("\u20BD101.60")
        );
        MatcherAssert.assertThat(
            Pair.valueOf("RUR 5111222333.0006"),
            Matchers.hasToString("\u20BD5111222333.00")
        );
        MatcherAssert.assertThat(
            Pair.valueOf("(GBP 555555.01)"),
            Matchers.hasToString("(\u00a3555555.01)")
        );
        MatcherAssert.assertThat(
            Pair.valueOf("$13.9").precision(2),
            Matchers.hasToString("$13.90")
        );
        MatcherAssert.assertThat(
            Pair.valueOf("\u20ac17.700"),
            Matchers.hasToString("\u20ac17.70")
        );
    }

    /**
     * Pair can be added to another cash.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsValueToAnotherObject() throws Exception {
        MatcherAssert.assertThat(
            Pair.valueOf("USD 10").add(Pair.valueOf("(USD 5.76)")),
            Matchers.equalTo(Pair.valueOf("USD 4.24"))
        );
    }

    /**
     * Pair can be multiplied.
     * @throws Exception If some problem inside
     */
    @Test
    public void multipliesItself() throws Exception {
        MatcherAssert.assertThat(
            Pair.valueOf("USD 1000").mul(5L).div(10L).div(10L),
            Matchers.equalTo(Pair.valueOf("USD 50"))
        );
    }

    /**
     * Pair can be divided.
     * @throws Exception If some problem inside
     */
    @Test
    public void dividesItself() throws Exception {
        MatcherAssert.assertThat(
            Pair.valueOf("USD 992").div(6L).toString(),
            Matchers.equalTo("$165.33")
        );
    }

    /**
     * Pair can be equal to itself.
     * @throws Exception If some problem inside
     */
    @Test
    public void equalsToItself() throws Exception {
        final String money = "(EUR 109.89)";
        MatcherAssert.assertThat(
            Pair.valueOf(money),
            Matchers.equalTo(Pair.valueOf(money))
        );
        MatcherAssert.assertThat(
            Pair.valueOf(money).hashCode(),
            Matchers.not(Matchers.equalTo(Pair.valueOf("EUR 40.2").hashCode()))
        );
        MatcherAssert.assertThat(
            Pair.valueOf(money),
            Matchers.not(Matchers.equalTo(Pair.valueOf("EUR 33.65")))
        );
    }

    /**
     * Pair can handle zero value gracefully.
     * @throws Exception If some problem inside
     */
    @Test
    public void handlesZeroValueGracefully() throws Exception {
        MatcherAssert.assertThat(
            new Pair().isEmpty(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Pair(new BigDecimal("0.000000"), Currency.EUR),
            Matchers.equalTo(new Pair())
        );
        MatcherAssert.assertThat(
            Pair.valueOf(new Pair().toString()).isEmpty(),
            Matchers.is(true)
        );
    }

    /**
     * Pair can change currencies.
     * @throws Exception If some problem inside
     */
    @Test
    public void exchangesUsingQuotes() throws Exception {
        final Quotes quotes = Mockito.mock(Quotes.class);
        Mockito.doReturn(1.25d).when(quotes).quote(Currency.EUR, Currency.USD);
        MatcherAssert.assertThat(
            Pair.valueOf("EUR 5").exchange(Currency.USD, quotes),
            Matchers.equalTo(Pair.valueOf("USD 6.25"))
        );
    }

    /**
     * Pair can be serialized.
     * @throws Exception If some problem inside
     */
    @Test
    public void serializesItselfAndBack() throws Exception {
        final Pair pair = Pair.valueOf("EUR 99.890");
        final byte[] data = SerializationUtils.serialize(pair);
        MatcherAssert.assertThat(
            Pair.class.cast(SerializationUtils.deserialize(data)),
            Matchers.equalTo(Pair.valueOf("EUR 99.89"))
        );
    }

    /**
     * Pair can convert to BigDecimal.
     * @throws Exception If some problem inside
     */
    @Test
    public void convertsToBigDecimal() throws Exception {
        MatcherAssert.assertThat(
            new Pair(new BigDecimal("57.43011"), Currency.EUR).decimal(),
            Matchers.equalTo(new BigDecimal("57.43"))
        );
    }

    /**
     * Pair can print fractions correctly.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsFractionsCorrectly() throws Exception {
        final String text = "$0.05";
        MatcherAssert.assertThat(
            Pair.valueOf(text).toString(),
            Matchers.equalTo(text)
        );
        MatcherAssert.assertThat(
            Pair.valueOf("$0.3").toString(),
            Matchers.equalTo("$0.30")
        );
    }

}
