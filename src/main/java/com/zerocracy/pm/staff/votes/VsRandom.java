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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.pm.staff.Votes;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Random rate.
 * <p>
 * Returns double value between 0.0 and 1.0
 *
 * @since 1.0
 */
public final class VsRandom implements Votes {
    /**
     * Random.
     */
    private final Random rnd;

    /**
     * Default constructor with {@link SecureRandom}.
     */
    public VsRandom() {
        this(new SecureRandom());
    }

    /**
     * Ctor.
     * @param random Random source.
     */
    public VsRandom(final Random random) {
        this.rnd = random;
    }

    @Override
    public double take(final String login, final StringBuilder log) {
        log.append("Entropy");
        return this.rnd.nextDouble();
    }
}
