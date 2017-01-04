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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Slow project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class SlowProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Closing executor.
     */
    private final ExecutorService service;

    /**
     * Ctor.
     * @param pkt Project
     */
    SlowProject(final Project pkt) {
        this.origin = pkt;
        this.service = Executors.newCachedThreadPool();
    }

    @Override
    public Item acq(final String file) throws IOException {
        return new SlowProject.SlowItem(this.origin.acq(file));
    }

    /**
     * Item that closes only after all acquirers call close().
     */
    private final class SlowItem implements Item {
        /**
         * Original item.
         */
        private final Item item;
        /**
         * Ctor.
         * @param itm Original item
         */
        SlowItem(final Item itm) {
            this.item = itm;
        }
        @Override
        public Path path() throws IOException {
            return this.item.path();
        }
        @Override
        public void close() {
            SlowProject.this.service.submit(
                () -> {
                    TimeUnit.SECONDS.sleep(1L);
                    this.item.close();
                    return null;
                }
            );
        }
    }
}
