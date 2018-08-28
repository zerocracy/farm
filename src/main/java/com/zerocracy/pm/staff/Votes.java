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
package com.zerocracy.pm.staff;

import java.io.IOException;

/**
 * Voter.
 *
 * @since 1.0
 */
public interface Votes {

    /**
     * Vote.
     * @param login GitHub login of the user
     * @param log Log of the take, if any
     * @return Points to give to him (0..1)
     * @throws IOException If fails
     */
    double take(String login, StringBuilder log) throws IOException;

    /**
     * Fake vote.
     */
    final class Fake implements Votes {

        /**
         * Vote.
         */
        private final double value;

        /**
         * Ctor.
         *
         * @param vote Vote
         */
        public Fake(final double vote) {
            this.value = vote;
        }

        @Override
        public double take(final String login, final StringBuilder log) {
            return this.value;
        }
    }
}
