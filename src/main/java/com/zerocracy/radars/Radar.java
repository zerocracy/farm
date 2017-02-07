/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.radars;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

/**
 * Radar.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
public interface Radar extends Closeable {

    /**
     * Start.
     * @throws IOException If fails
     */
    void start() throws IOException;

    /**
     * Reactions chained.
     */
    final class Chain implements Radar {
        /**
         * Radars.
         */
        private final Iterable<Radar> radars;
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Iterable<Radar> list) {
            this.radars = list;
        }
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Radar... list) {
            this(Arrays.asList(list));
        }
        @Override
        public void start() throws IOException {
            for (final Radar radar : this.radars) {
                radar.start();
            }
        }
        @Override
        public void close() throws IOException {
            for (final Radar radar : this.radars) {
                radar.close();
            }
        }
    }

}
