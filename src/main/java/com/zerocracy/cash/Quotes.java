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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Quotes.
 *
 * @since 1.0
 */
public interface Quotes {

    /**
     * Simple implementation.
     * @checkstyle AnonInnerLength (50 lines)
     */
    Quotes DEFAULT = new Quotes() {
        /**
         * Exchange rates.
         */
        @SuppressWarnings("PMD.NonStaticInitializer")
        private final ConcurrentMap<Currency, Double> rates =
            new ConcurrentHashMap<Currency, Double>() {
                private static final long serialVersionUID = 0x7523CA78C1DAL;
                {
                    // @checkstyle MagicNumber (7 lines)
                    this.put(Currency.USD, 1.0d);
                    this.put(Currency.EUR, 1.32d);
                    this.put(Currency.RUR, 0.017d);
                    this.put(Currency.GBP, 1.59d);
                    this.put(Currency.JPY, 0.011d);
                }
            };
        @Override
        public double quote(final Currency src, final Currency dest) {
            final Double sell = this.rates.get(src);
            final Double buy = this.rates.get(dest);
            return sell * (1.0d / buy);
        }
    };

    /**
     * Get a quote.
     * @param src Source currency
     * @param dest Destination currency
     * @return The quote (exchange rate)
     * @throws IOException If fails due to I/O problems
     */
    double quote(Currency src, Currency dest) throws IOException;

}
