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
package com.zerocracy.pm.staff.votes;

import com.jcabi.log.Logger;
import com.zerocracy.pm.staff.Votes;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Measured votes.
 *
 * @since 1.0
 */
public final class VsMeasured implements Votes {

    /**
     * Votes.
     */
    private final Votes vts;

    /**
     * Timer.
     */
    private final AtomicLong timer;

    /**
     * Ctor.
     *
     * @param vts Origin votes
     */
    public VsMeasured(final Votes vts) {
        this.vts = vts;
        this.timer = new AtomicLong();
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final long start = System.nanoTime();
        final double res = this.vts.take(login, log);
        this.timer.addAndGet(System.nanoTime() - start);
        return res;
    }

    @Override
    public String toString() {
        return Logger.format(
            "[%s %[nano]s]",
            this.vts.getClass().getSimpleName(),
            this.timer.get()
        );
    }
}
