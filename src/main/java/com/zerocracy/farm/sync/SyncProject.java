/*
 * Copyright (c) 2016-2019 Zerocracy
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
import com.zerocracy.Project;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import lombok.EqualsAndHashCode;
import org.cactoos.Func;
import org.cactoos.Proc;

/**
 * Sync project.
 *
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
final class SyncProject implements Project {

    /**
     * Does strict projects enabled.
     */
    private static final boolean STRICT_ENABLED = false;

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Locks.
     */
    private final Locks locks;

    /**
     * Lock flags for projects artifacts.
     */
    private final ConcurrentMap<String, AtomicBoolean> lpkt;

    /**
     * Ctor.
     * @param pkt Project
     * @param lcks Locks
     * @param lpkt Project lock flags
     */
    SyncProject(final Project pkt, final Locks lcks,
        final ConcurrentMap<String, AtomicBoolean> lpkt) {
        this.origin = pkt;
        this.locks = lcks;
        this.lpkt = lpkt;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final ReadWriteLock lock = this.locks.lock(this, file);
        final Item item;
        if (SyncProject.STRICT_ENABLED) {
            item = new SyncProject.StrictProjectItem(
                this.origin.acq(file),
                this.lpkt.computeIfAbsent(file, key -> new AtomicBoolean())
            );
        } else {
            item = new WarnItem(
                String.format("%s/%s", this.pid(), file),
                new SyncItem(this.origin.acq(file), lock)
            );
        }
        return item;
    }

    /**
     * Strict project.
     */
    private static final class StrictProjectItem implements Item {

        /**
         * Origin item.
         */
        private final Item item;

        /**
         * Acquire flag.
         */
        private final AtomicBoolean acq;

        /**
         * Ctor.
         * @param item Item
         * @param acq Flag
         */
        StrictProjectItem(final Item item, final AtomicBoolean acq) {
            this.item = item;
            this.acq = acq;
        }

        @Override
        @SuppressWarnings("PMD.PrematureDeclaration")
        public <T> T read(final Func<Path, T> reader) throws IOException {
            if (!this.acq.compareAndSet(false, true)) {
                throw new IOException(
                    String.format(
                        "read: unable to access locked item: %s", this.item
                    )
                );
            }
            final T res = this.item.read(reader);
            if (!this.acq.compareAndSet(true, false)) {
                throw new IOException(
                    String.format(
                        "read: unable to free unlocked item: %s", this.item
                    )
                );
            }
            return res;
        }

        @Override
        public void update(final Proc<Path> writer) throws IOException {
            if (!this.acq.compareAndSet(false, true)) {
                throw new IOException(
                    String.format(
                        "write: unable to access locked item: %s", this.item
                    )
                );
            }
            this.item.update(writer);
            if (!this.acq.compareAndSet(true, false)) {
                throw new IOException(
                    String.format(
                        "write: unable to free unlocked item: %s", this.item
                    )
                );
            }
        }
    }
}
