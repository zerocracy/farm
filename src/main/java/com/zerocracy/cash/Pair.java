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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;

/**
 * Pair.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
final class Pair implements Comparable<Pair>, Serializable {

    /**
     * Comparator for ABC sorting.
     * @checkstyle JavadocVariableCheck (3 lines)
     */
    public static final Comparator<Pair> COMPARATOR = (left, right) -> {
        int diff = right.compareTo(left);
        if (diff == 0) {
            diff = left.currency.compareTo(right.currency);
        }
        return diff;
    };

    /**
     * Pattern.
     */
    private static final Pattern FMT =
        Pattern.compile("0|([A-Z]{3}|.) *([0-9]+(?:\\.[0-9]+)?)");

    /**
     * Currencies by symbol.
     * @checkstyle DiamondOperatorCheck (5 lines)
     */
    private static final Map<Character, Currency> BY_SYMBOL =
        new SolidMap<Character, Currency>(
            new MapEntry<>(Currency.USD.symbol(), Currency.USD),
            new MapEntry<>(Currency.EUR.symbol(), Currency.EUR),
            new MapEntry<>(Currency.RUR.symbol(), Currency.RUR),
            new MapEntry<>(Currency.GBP.symbol(), Currency.GBP),
            new MapEntry<>(Currency.JPY.symbol(), Currency.JPY)
        );

    /**
     * Currencies by symbol.
     * @checkstyle DiamondOperatorCheck (5 lines)
     */
    private static final Map<String, Currency> BY_ISO =
        new SolidMap<String, Currency>(
            new MapEntry<>(Currency.USD.toString(), Currency.USD),
            new MapEntry<>(Currency.EUR.toString(), Currency.EUR),
            new MapEntry<>(Currency.RUR.toString(), Currency.RUR),
            new MapEntry<>(Currency.GBP.toString(), Currency.GBP),
            new MapEntry<>(Currency.JPY.toString(), Currency.JPY)
        );

    /**
     * Serialization ID.
     */
    private static final long serialVersionUID = 0x7A23FAA7CFDFA130L;

    /**
     * Exact value.
     * @serial
     */
    private final String value;

    /**
     * Precision, digits after dot to show.
     * @serial
     */
    private final int digits;

    /**
     * Three-letter currency code, as in ISO 4217.
     * @link <a href="http://en.wikipedia.org/wiki/ISO_4217">ISO 4217</a>
     * @serial
     */
    private final Currency currency;

    /**
     * Public ctor (zero value).
     */
    Pair() {
        this(BigDecimal.ZERO, Currency.USD);
    }

    /**
     * Public ctor (zero value).
     * @param val Value
     * @param crnc Currency
     */
    Pair(final BigDecimal val, final Currency crnc) {
        this(val, 2, crnc);
    }

    /**
     * Private ctor.
     * @param val Value
     * @param dgts Digits after dot
     * @param crnc Currency
     */
    private Pair(final BigDecimal val, final int dgts, final Currency crnc) {
        this.value = val.stripTrailingZeros().toString();
        this.digits = dgts;
        this.currency = crnc;
    }

    /**
     * Parse pair.
     * @param text Text presentation of a monetary pair
     * @return The pair found
     * @throws CashParsingException If fails to parse
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static Pair valueOf(final String text) throws CashParsingException {
        if (text.isEmpty()) {
            throw new CashParsingException(
                "It's an empty string, not a cash value"
            );
        }
        final boolean negative;
        final Matcher matcher;
        if (text.charAt(0) == '(' && text.charAt(text.length() - 1) == ')') {
            negative = true;
            matcher = Pair.FMT.matcher(text.substring(1, text.length() - 1));
        } else {
            negative = false;
            matcher = Pair.FMT.matcher(text);
        }
        if (!matcher.matches()) {
            throw new CashParsingException(
                String.format(
                    "Can't parse cash value pair \"%s\"", text
                )
            );
        }
        final Pair pair;
        if (matcher.group(1) == null) {
            pair = new Pair();
        } else {
            BigDecimal val = new BigDecimal(matcher.group(2));
            if (negative) {
                val = val.negate();
            }
            try {
                pair = new Pair(val, Pair.parse(matcher.group(1)));
            } catch (final CashParsingException ex) {
                throw new CashParsingException(
                    String.format("Can't parse \"%s\"", text),
                    ex
                );
            }
        }
        return pair;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean equals;
        if (obj instanceof Pair) {
            final Pair pair = Pair.class.cast(obj);
            equals = this.isEmpty()
                && pair.isEmpty()
                || this.value.equals(pair.value)
                && this.currency.equals(pair.currency);
        } else {
            equals = false;
        }
        return equals;
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(10);
        final BigDecimal decimal = this.decimal();
        final String numbers = decimal.abs()
            .setScale(this.digits, RoundingMode.HALF_UP)
            .round(MathContext.DECIMAL64)
            .toString();
        if (numbers.matches("0\\.0+")) {
            text.append('0');
        } else {
            if (decimal.signum() < 0) {
                text.append('(');
            }
            text.append(this.currency.symbol()).append(numbers);
            if (decimal.signum() < 0) {
                text.append(')');
            }
        }
        return text.toString();
    }

    @Override
    public int compareTo(final Pair pair) {
        return this.decimal().compareTo(pair.decimal());
    }

    /**
     * Is it empty?
     * @return TRUE if it's zero
     */
    public boolean isEmpty() {
        return this.decimal().compareTo(new BigDecimal("0.0")) == 0;
    }

    /**
     * Two pairs are comparable, if they have the same currency.
     * @param pair The pair to check
     * @return TRUE if they have the same currency
     */
    public boolean comparable(final Pair pair) {
        return this.currency.equals(pair.currency);
    }

    /**
     * Convert it to {@link BigDecimal}.
     * @return Big decimal
     */
    public BigDecimal decimal() {
        return new BigDecimal(this.value)
            .setScale(this.digits, RoundingMode.HALF_UP)
            .stripTrailingZeros();
    }

    /**
     * Join with another pair (in the same currency).
     * @param pair The pair to join into
     * @return New pair
     */
    public Pair add(final Pair pair) {
        if (!this.comparable(pair)) {
            throw new IllegalArgumentException(
                String.format(
                    "Currencies mismatch: \"%s\" vs \"%s\"",
                    this, pair
                )
            );
        }
        return new Pair(
            this.decimal().add(pair.decimal()),
            this.digits, this.currency
        );
    }

    /**
     * Multiply by and return a new pair.
     * @param multiplier Multiplier
     * @return New cash pair
     */
    public Pair mul(final long multiplier) {
        return new Pair(
            this.decimal().multiply(new BigDecimal(multiplier)),
            this.digits,
            this.currency
        );
    }

    /**
     * Divide by and return a new pair.
     * @param divider Divider
     * @return New cash pair
     */
    public Pair div(final long divider) {
        if (divider == 0L) {
            throw new IllegalArgumentException(
                "Divider can't be zero"
            );
        }
        return new Pair(
            this.decimal().divide(
                new BigDecimal(divider),
                // @checkstyle MagicNumber (1 line)
                6,
                RoundingMode.HALF_UP
            ),
            this.digits,
            this.currency
        );
    }

    /**
     * Change precision.
     * @param dgts Digits after dot
     * @return New cash pair
     */
    public Pair precision(final int dgts) {
        return new Pair(this.decimal(), dgts, this.currency);
    }

    /**
     * Exchange the entire cash to one currency.
     * @param dest The currency to exchange to
     * @param quotes The quotes to use
     * @return New pair
     * @throws IOException If fails due to I/O problems
     */
    public Pair exchange(final Currency dest, final Quotes quotes)
        throws IOException {
        final Pair pair;
        if (dest.equals(this.currency)) {
            pair = this;
        } else {
            pair = new Pair(
                this.decimal().multiply(
                    BigDecimal.valueOf(quotes.quote(this.currency, dest))
                ),
                this.digits,
                dest
            );
        }
        return pair;
    }

    /**
     * Parse text and return currency.
     * @param text Text to parse
     * @return Currency found
     * @throws CashParsingException If fails to parse
     */
    private static Currency parse(final String text)
        throws CashParsingException {
        final Currency currency;
        if (text.length() == 1) {
            currency = Pair.BY_SYMBOL.get(text.charAt(0));
            if (currency == null) {
                throw new CashParsingException(
                    String.format(
                        "Invalid currency symbol \"%s\"", text
                    )
                );
            }
        } else if (text.matches("[A-Z]{3}")) {
            currency = Pair.BY_ISO.get(text);
            if (currency == null) {
                throw new CashParsingException(
                    String.format(
                        "Unsupported currency ISO code \"%s\"", text
                    )
                );
            }
        } else {
            throw new CashParsingException(
                String.format(
                    "Invalid currency \"%s\"", text
                )
            );
        }
        return currency;
    }

}
