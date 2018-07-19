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

import java.io.Serializable;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Currency}.
 * @since 1.0
 */
public final class CurrencyTest {

    /**
     * Currency can be serialized.
     * @throws Exception If some problem inside
     */
    @Test
    public void serializesItselfAndBack() throws Exception {
        final Serializable currency = Currency.USD;
        final byte[] data = SerializationUtils.serialize(currency);
        MatcherAssert.assertThat(
            Currency.class.cast(SerializationUtils.deserialize(data)),
            Matchers.equalTo(Currency.USD)
        );
    }

    /**
     * Currency can be compared to itself.
     * @throws Exception If some problem inside
     */
    @Test
    public void comparesToItself() throws Exception {
        MatcherAssert.assertThat(
            Currency.EUR.hashCode(),
            Matchers.equalTo(Currency.EUR.hashCode())
        );
        MatcherAssert.assertThat(
            Currency.GBP,
            Matchers.equalTo(Currency.GBP)
        );
    }

}
