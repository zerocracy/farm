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
package com.zerocracy.farm.reactive;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spinner for the spin.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
final class RunnableWithTrigger implements Runnable {

    /**
     * The runnable.
     */
    private final Runnable runnable;

    /**
     * Is it running now?
     */
    private final AtomicBoolean alive;

    /**
     * Ctor.
     * @param rnb Runnable
     * @param alv Alive flag
     */
    RunnableWithTrigger(final Runnable rnb, final AtomicBoolean alv) {
        this.runnable = rnb;
        this.alive = alv;
    }

    @Override
    public void run() {
        this.runnable.run();
        this.alive.set(false);
    }

}
