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
package com.zerocracy.pulse;

/**
 * {@link Pulse} tick.
 *
 * @since 1.0
 */
public final class Tick {

    /**
     * When was it started.
     */
    private final long date;

    /**
     * Duration.
     */
    private final long msec;

    /**
     * Footprint items processed.
     */
    private final int items;

    /**
     * Ctor.
     * @param date When
     * @param msec Duration in msec
     * @param total Total processed
     */
    public Tick(final long date, final long msec,
        final int total) {
        this.date = date;
        this.msec = msec;
        this.items = total;
    }

    /**
     * Time of start.
     * @return Time of start
     */
    public long start() {
        return this.date;
    }

    /**
     * Duration in msec.
     * @return Duration
     */
    public long duration() {
        return this.msec;
    }

    /**
     * Total processed.
     * @return Number of footprint items
     */
    public int total() {
        return this.items;
    }

}
