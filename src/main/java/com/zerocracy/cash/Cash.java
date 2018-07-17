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
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import lombok.EqualsAndHashCode;

/**
 * Cash.
 *
 * @since 1.0
 * @checkstyle TooManyMethods (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface Cash extends Comparable<Cash>, Serializable {

    /**
     * Zero.
     */
    Cash ZERO = new Cash.S();

    /**
     * Returns true if this cash value equals to zero.
     * @return TRUE if empty
     */
    boolean isEmpty();

    /**
     * Add another cash to it.
     *
     * @param cash Cash to add
     * @return New cash
     */
    Cash add(Cash cash);

    /**
     * Multiply by.
     *
     * @param multiplier Multiplier
     * @return New cash
     */
    Cash mul(long multiplier);

    /**
     * Divide by.
     *
     * @param divider Divider
     * @return New cash
     */
    Cash div(long divider);

    /**
     * Divide by.
     *
     * @param divider Divider
     * @return Ratio
     */
    double div(Cash divider);

    /**
     * Convert it to {@link BigDecimal}.
     * @return Big decimal
     */
    BigDecimal decimal();

    /**
     * Use this quotes.
     * @param quotes The quotes to use
     * @return New cash that uses given quotes
     */
    Cash quotes(Quotes quotes);

    /**
     * Use this precision.
     * @param digits Digits after dot
     * @return New cash that uses new precision
     */
    Cash precision(int digits);

    /**
     * Exchange the entire cash to one currency.
     *
     * @param currency The currency to exchange to
     * @return New cash, all in one currency
     * @throws IOException If fails due to I/O problems
     */
    Cash exchange(Currency currency) throws IOException;

    /**
     * Is cash representation unified (there were no {@code add} method called)?
     *
     * Non-unified Cash object is created by doing {@link #add(Cash)} of
     * {@code Cash} objects:
     * <pre>
     *     Cash cash = new Cash.S("$10").add(new Cash.S("$20"));
     * </pre>
     *
     * To get a unified {@code Cash} object you need to call
     * {@link #exchange(Currency)} method.
     * @return True if the cash has been unified.
     */
    boolean unified();

    /**
     * Simple implementation.
     */
    @EqualsAndHashCode(of = "pairs")
    final class S implements Cash {

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 0x7523CA77C1DF0032L;

        /**
         * Pairs in order.
         */
        private final Pair[] pairs;

        /**
         * Quotes to use for comparison.
         */
        private final transient Quotes qts;

        /**
         * Public ctor, with zero monetary value.
         */
        public S() {
            this(new Pair[] {new Pair()}, Quotes.DEFAULT);
        }

        /**
         * Public ctor.
         * @param text Text presentation of a monetary value
         * @throws CashParsingException If fails to parse
         */
        public S(final String text) throws CashParsingException {
            this(Cash.S.parse(text), Quotes.DEFAULT);
        }

        /**
         * Private ctor.
         * @param prs Pairs
         * @param quotes Quotes to use
         */
        private S(final Pair[] prs, final Quotes quotes) {
            this.pairs = Cash.S.normalized(prs);
            this.qts = quotes;
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder(0);
            for (final Pair pair : this.pairs) {
                if (text.length() > 0) {
                    text.append(" + ");
                }
                text.append(pair);
            }
            return text.toString();
        }

        @Override
        public int compareTo(final Cash cash) {
            try {
                return Pair.valueOf(this.exchange(Currency.USD).toString())
                    .compareTo(
                        Pair.valueOf(cash.exchange(Currency.USD).toString())
                    );
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public boolean isEmpty() {
            return this.pairs.length == 1 && this.pairs[0].isEmpty();
        }

        @Override
        public Cash add(final Cash cash) {
            final Cash.S friend = Cash.S.class.cast(cash);
            final Pair[] prs =
                new Pair[this.pairs.length + friend.pairs.length];
            System.arraycopy(
                this.pairs, 0,
                prs, 0,
                this.pairs.length
            );
            System.arraycopy(
                friend.pairs, 0,
                prs, this.pairs.length,
                friend.pairs.length
            );
            return new Cash.S(prs, this.qts);
        }

        @Override
        public Cash mul(final long multiplier) {
            final Pair[] prs = new Pair[this.pairs.length];
            for (int num = 0; num < this.pairs.length; ++num) {
                prs[num] = this.pairs[num].mul(multiplier);
            }
            return new Cash.S(prs, this.qts);
        }

        @Override
        public Cash div(final long divider) {
            if (divider == 0) {
                throw new IllegalArgumentException(
                    "Divider can't be zero"
                );
            }
            final Pair[] prs = new Pair[this.pairs.length];
            for (int num = 0; num < this.pairs.length; ++num) {
                prs[num] = this.pairs[num].div(divider);
            }
            return new Cash.S(prs, this.qts);
        }

        @Override
        public double div(final Cash divider) {
            if (divider.equals(Cash.ZERO)) {
                throw new IllegalArgumentException(
                    "Cash divider can't be zero"
                );
            }
            return this.decimal().doubleValue()
                / divider.decimal().doubleValue();
        }

        @Override
        public BigDecimal decimal() {
            if (!this.unified()) {
                throw new IllegalStateException(
                    String.format(
                        "Use exchange() first to unify currencies: \"%s\"",
                        this
                    )
                );
            }
            return this.pairs[0].decimal();
        }

        @Override
        public Cash quotes(final Quotes quotes) {
            return new Cash.S(this.pairs, quotes);
        }

        @Override
        public Cash precision(final int digits) {
            final Pair[] prs = new Pair[this.pairs.length];
            for (int num = 0; num < this.pairs.length; ++num) {
                prs[num] = this.pairs[num].precision(digits);
            }
            return new Cash.S(prs, this.qts);
        }

        @Override
        public Cash exchange(final Currency currency)
            throws IOException {
            final Pair[] prs = new Pair[this.pairs.length];
            for (int num = 0; num < this.pairs.length; ++num) {
                prs[num] = this.pairs[num].exchange(currency, this.qts);
            }
            return new Cash.S(prs, this.qts);
        }

        @Override
        public boolean unified() {
            return this.pairs.length == 1;
        }

        /**
         * Parse text and return an array of pairs.
         * @param text The text to parse
         * @return Array of pairs
         * @throws CashParsingException If fails to parse
         */
        private static Pair[] parse(final String text)
            throws CashParsingException {
            final String[] parts = text.split(" \\+ ");
            final Pair[] prs = new Pair[parts.length];
            for (int num = 0; num < parts.length; ++num) {
                prs[num] = Pair.valueOf(parts[num]);
            }
            return prs;
        }

        /**
         * Normalized pairs.
         * @param prs Pairs
         * @return Pairs
         */
        private static Pair[] normalized(final Pair... prs) {
            assert prs.length > 0;
            final Pair[] buf = new Pair[prs.length];
            int pos = 0;
            for (final Pair pair : prs) {
                if (pair.isEmpty()) {
                    continue;
                }
                boolean added = false;
                for (int left = 0; left < pos; ++left) {
                    if (buf[left].comparable(pair)) {
                        buf[left] = buf[left].add(pair);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    buf[pos] = pair;
                    ++pos;
                }
            }
            final Pair[] array;
            if (pos == 0) {
                array = new Pair[] {new Pair()};
            } else {
                array = new Pair[pos];
                System.arraycopy(buf, 0, array, 0, array.length);
                Arrays.sort(array, Pair.COMPARATOR);
            }
            return array;
        }

    }
}
