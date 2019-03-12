/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.tools;

import java.security.SecureRandom;
import java.util.Random;
import org.cactoos.Text;

/**
 * Random string.
 *
 * @since 1.0
 */
public final class RandomString implements Text {

    /**
     * Random generator.
     */
    private static final SecureRandom RND = new SecureRandom();

    /**
     * Default symbols.
     * @checkstyle LineLengthCheck (3 lines)
     */
    private static final String SRC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Random generator.
     */
    private final Random random;

    /**
     * Source symbols.
     */
    private final char[] symbols;

    /**
     * String length.
     */
    private final int len;

    /**
     * Ctor.
     * @param length String length
     * @param random Random generator
     * @param symbols Symbols
     */
    public RandomString(final int length, final Random random,
        final String symbols) {
        this.random = random;
        this.symbols = symbols.toCharArray();
        this.len = length;
    }

    /**
     * Ctor.
     * @param length String length
     * @param random Random
     */
    public RandomString(final int length, final Random random) {
        this(length, random, RandomString.SRC);
    }

    /**
     * Ctor.
     * @param length String length
     */
    public RandomString(final int length) {
        this(length, RandomString.RND);
    }

    @Override
    public String asString() {
        final char[] buf = new char[this.len];
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = this.symbols[this.random.nextInt(this.symbols.length)];
        }
        return new String(buf);
    }
}
