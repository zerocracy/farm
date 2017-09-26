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
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.jstk.Item;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * Synchronized and thread safe item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@EqualsAndHashCode(of = "origin")
final class SyncItem implements Item {

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * Semaphore.
     */
    private final Semaphore semaphore;

    /**
     * Ctor.
     * @param item Original item
     */
    SyncItem(final Item item) {
        this.origin = item;
        this.semaphore = new Semaphore(1, true);
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        if (this.semaphore.availablePermits() > 0) {
            throw new IllegalStateException(
                String.format(
                    "The item \"%s\" is closed already",
                    this.origin
                )
            );
        }
        synchronized (this.origin) {
            return this.origin.path();
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this.origin) {
            this.origin.close();
            this.semaphore.release();
        }
    }

    /**
     * Acquire access.
     * @throws InterruptedException If fails
     */
    public void acquire() throws InterruptedException {
        final long start = System.currentTimeMillis();
        if (!this.semaphore.tryAcquire((long) Tv.TWENTY, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                Logger.format(
                    "Failed to acquire \"%s\" in %[ms]s",
                    this.origin,
                    System.currentTimeMillis() - start
                )
            );
        }
    }

}
