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
import java.io.IOException;

/**
 * Votes that always return [0..1].
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VsSafe implements Votes {

    /**
     * Original votes.
     */
    private final Votes origin;

    /**
     * Ctor.
     * @param votes Original votes
     */
    public VsSafe(final Votes votes) {
        this.origin = votes;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final double vote = this.origin.take(login, log);
        if (vote < 0.0d) {
            throw new IllegalStateException(
                String.format(
                    "Vote can't be negative: %f", vote
                )
            );
        }
        if (vote > 1.0d) {
            throw new IllegalStateException(
                String.format(
                    "Vote can't be over 1.0: %f", vote
                )
            );
        }
        return vote;
    }
}
