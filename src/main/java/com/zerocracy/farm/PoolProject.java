/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.farm;

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pool project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class PoolProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Pool of items.
     */
    private final Map<String, PoolProject.PoolItem> pool;

    /**
     * Ctor.
     * @param pkt Project
     */
    PoolProject(final Project pkt) {
        this.origin = pkt;
        this.pool = new HashMap<>(0);
    }

    @Override
    public Item acq(final String file) throws IOException {
        if (!this.pool.containsKey(file)) {
            this.pool.put(
                file,
                new PoolProject.PoolItem(this.origin.acq(file))
            );
        }
        final PoolProject.PoolItem item = this.pool.get(file);
        item.increment();
        return item;
    }

    /**
     * Item that closes only after all acquirers call close().
     */
    private static final class PoolItem implements Item {
        /**
         * Original item.
         */
        private final Item origin;
        /**
         * Counter of requests.
         */
        private final AtomicInteger requests;
        /**
         * Ctor.
         * @param item Original item
         */
        PoolItem(final Item item) {
            this.origin = item;
            this.requests = new AtomicInteger();
        }
        @Override
        public Path path() throws IOException {
            this.requests.incrementAndGet();
            return this.origin.path();
        }
        @Override
        public void close() throws IOException {
            if (this.requests.decrementAndGet() == 0) {
                this.origin.close();
            }
            if (this.requests.get() < 0) {
                throw new IllegalStateException(
                    "something is wrong, you close() too much"
                );
            }
        }
        /**
         * Increment request counter.
         */
        public void increment() {
            this.requests.incrementAndGet();
        }
    }
}
