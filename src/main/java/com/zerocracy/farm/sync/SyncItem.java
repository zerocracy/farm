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
package com.zerocracy.farm.sync;

import com.zerocracy.Item;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import lombok.EqualsAndHashCode;

/**
 * Synchronized and thread safe item.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
final class SyncItem implements Item {

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * Lock.
     */
    private final Lock lock;

    /**
     * Ctor.
     * @param item Original item
     * @param lck Lock
     */
    SyncItem(final Item item, final Lock lck) {
        this.origin = item;
        this.lock = lck;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        if (Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                String.format(
                    "The thread %s is interrupted, can't continue.",
                    Thread.currentThread().getName()
                )
            );
        }
        return this.origin.path();
    }

    @Override
    public void close() throws IOException {
        try {
            this.origin.close();
        } finally {
            this.lock.unlock();
        }
    }

}
