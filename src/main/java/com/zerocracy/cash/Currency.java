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
import lombok.EqualsAndHashCode;

/**
 * Currency.
 *
 * @since 1.0
 */
public interface Currency extends Comparable<Currency>, Serializable {

    /**
     * United States Dollar (USD).
     */
    Currency USD = new Currency.S("USD", '$');

    /**
     * Euro (EUR).
     */
    Currency EUR = new Currency.S("EUR", '\u20ac');

    /**
     * Ruble (RUR).
     */
    Currency RUR = new Currency.S("RUR", '\u20BD');

    /**
     * Great Britain Pound (GBP).
     */
    Currency GBP = new Currency.S("GBP", '\u00a3');

    /**
     * Japanese Yen (JPY).
     */
    Currency JPY = new Currency.S("JPY", '\u00a5');

    /**
     * Get its ISO 4217 code.
     * @return The ISO code
     */
    String code();

    /**
     * Get its ISO 4217 symbol.
     * @return The ISO symbol
     */
    char symbol();

    /**
     * Simple implementation.
     */
    @EqualsAndHashCode(of = "iso")
    final class S implements Currency {
        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 0x7523FA77CFDF0130L;
        /**
         * Three-letter currency code, as in ISO 4217.
         * @see <a href="http://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
         * @serial
         */
        private final String iso;
        /**
         * One-letter symbol, as in ISO 4217.
         * @serial
         */
        private final char sym;
        /**
         * Public ctor.
         * @param code ISO 4271 currency iso
         * @param symbol Symbol
         */
        public S(final String code, final char symbol) {
            this.iso = code;
            this.sym = symbol;
        }
        @Override
        public String toString() {
            return this.iso;
        }
        @Override
        public int compareTo(final Currency currency) {
            return this.toString().compareTo(currency.toString());
        }
        @Override
        public String code() {
            return this.iso;
        }
        @Override
        public char symbol() {
            return this.sym;
        }
    }

}
