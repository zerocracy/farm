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
package com.zerocracy.pm.staff.ranks;

import com.jcabi.log.Logger;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Measured rank.
 *
 * @since 1.0
 */
public final class RnkMeasured implements Comparator<String> {

    /**
     * Rank.
     */
    private final Comparator<String> rnk;

    /**
     * Timer.
     */
    private final AtomicLong timer;

    /**
     * Ctor.
     *
     * @param rnk Origin rank
     */
    public RnkMeasured(final Comparator<String> rnk) {
        this.rnk = rnk;
        this.timer = new AtomicLong();
    }

    @Override
    public int compare(final String left, final String right) {
        final long start = System.nanoTime();
        final int res = this.rnk.compare(left, right);
        this.timer.addAndGet(System.nanoTime() - start);
        return res;
    }

    @Override
    public String toString() {
        return Logger.format(
            "[%s %[nano]s]",
            this.rnk.getClass().getSimpleName(),
            this.timer.get()
        );
    }
}
