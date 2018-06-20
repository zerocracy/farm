/**
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

import com.jcabi.log.Logger;
import com.zerocracy.Item;
import com.zerocracy.Project;
import io.sentry.Sentry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import lombok.EqualsAndHashCode;

/**
 * Sync project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.1
 */
@EqualsAndHashCode(of = "origin")
final class SyncProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Lock.
     */
    private final Lock lock;

    /**
     * Ctor.
     * @param pkt Project
     * @param lck Lock
     */
    SyncProject(final Project pkt, final Lock lck) {
        this.origin = pkt;
        this.lock = lck;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final long start = System.currentTimeMillis();
        try {
            // @checkstyle MagicNumber (1 line)
            if (!this.lock.tryLock(2L, TimeUnit.MINUTES)) {
                throw new IllegalStateException(
                    Logger.format(
                        "Failed to acquire \"%s\" in \"%s\" in %[ms]s: %s",
                        file, this.origin.pid(),
                        System.currentTimeMillis() - start,
                        this.lock
                    )
                );
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                Logger.format(
                    "%s interrupted while waiting for \"%s\" in %s for %[ms]s",
                    Thread.currentThread().getName(),
                    file, this.pid(),
                    System.currentTimeMillis() - start
                ),
                ex
            );
        }
        final SyncProject.TrackItem item =
            new SyncProject.TrackItem(
                new SyncItem(this.origin.acq(file), this.lock)
            );
        final Exception exx = new IllegalStateException(
            String.format("Item '%s' was not closed for 2 minutes", file)
        );
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    if (!item.closed()) {
                        Sentry.capture(exx);
                    }
                }
            },
            // @checkstyle MagicNumber (1 line)
            TimeUnit.MINUTES.toMillis(2L)
        );
        return item;
    }

    /**
     * Track item close.
     */
    private static final class TrackItem implements Item {
        /**
         * Origin item.
         */
        private final Item itm;
        /**
         * Closed.
         */
        private final AtomicBoolean cls;
        /**
         * Ctor.
         * @param origin Origin item
         */
        private TrackItem(final Item origin) {
            this.itm = origin;
            this.cls = new AtomicBoolean(false);
        }

        @Override
        public Path path() throws IOException {
            return this.itm.path();
        }

        @Override
        public void close() throws IOException {
            if (this.cls.compareAndSet(false, true)) {
                this.itm.close();
            }
        }
        /**
         * Item closed.
         * @return true if closed
         */
        public boolean closed() {
            return this.cls.get();
        }
    }
}
