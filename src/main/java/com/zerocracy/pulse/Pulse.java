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

import java.util.Collections;

/**
 * Zerocracy pulse info.
 *
 * @since 1.0
 * @todo #1098:30min Wire pulse information from /pulse into Pulse as Ticks.
 *  See implementation in rultor project on how to initalize Pulse objetcs in
 *  program start and how to make it Tick every minute to collect execution
 *  data. Then add SVG created in TkTicks to start page (or wherever the
 *  pulse bar have to be shown).
 */
public interface Pulse {

    /**
     * Add new tick.
     * @param tick The tick
     */
    void add(Tick tick);

    /**
     * Get ticks.
     * @return Ticks
     */
    Iterable<Tick> ticks();

    /**
     * Most recent exception (or empty).
     * @return Problems
     */
    Iterable<Throwable> error();

    /**
     * Set recent exception (or empty).
     * @param errors Errors or empty if none
     */
    void error(Iterable<Throwable> errors);

    /**
     * Empty.
     */
    class Empty implements Pulse {

        @Override
        public void add(final Tick tick) {
            throw new UnsupportedOperationException("#add()");
        }

        @Override
        public Iterable<Tick> ticks() {
            return Collections.emptyList();
        }

        @Override
        public Iterable<Throwable> error() {
            return Collections.emptyList();
        }

        @Override
        public void error(final Iterable<Throwable> errors) {
            throw new UnsupportedOperationException("#error()");
        }
    };
}
