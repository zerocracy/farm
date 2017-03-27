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

import com.jcabi.log.Logger;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Pool project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class SyncProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Pool of items.
     */
    private final Map<String, SyncProject.Itm> pool;

    /**
     * Ctor.
     * @param pkt Project
     */
    SyncProject(final Project pkt) {
        this.origin = pkt;
        this.pool = new HashMap<>(0);
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        synchronized (this.pool) {
            if (!this.pool.containsKey(file)) {
                this.pool.put(
                    file,
                    new SyncProject.Itm(this.origin.acq(file))
                );
            }
        }
        final SyncProject.Itm item = this.pool.get(file);
        try {
            item.acquire();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        return new SyncItem(item);
    }

    /**
     * Item that closes only after all acquirers call close().
     */
    private static final class Itm implements Item {
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
        Itm(final Item item) {
            this.origin = item;
            this.semaphore = new Semaphore(1);
        }
        @Override
        public Path path() throws IOException {
            return this.origin.path();
        }
        @Override
        public void close() throws IOException {
            this.origin.close();
            Logger.info(this, "ACQ: released %s", this.origin);
            this.semaphore.release();
        }
        /**
         * Acquire access.
         * @throws InterruptedException If fails
         */
        public void acquire() throws InterruptedException {
            if (!this.semaphore.tryAcquire(1L, TimeUnit.MINUTES)) {
                throw new IllegalStateException(
                    String.format("Failed to acquire %s", this.origin)
                );
            }
            Logger.info(this, "ACQ: acquired %s", this.origin);
        }
    }
}
