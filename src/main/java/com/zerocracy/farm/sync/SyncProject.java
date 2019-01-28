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

import com.jcabi.log.Logger;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import lombok.EqualsAndHashCode;

/**
 * Sync project.
 *
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
final class SyncProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Locks.
     */
    private final Locks locks;

    /**
     * Lock time (millis).
     */
    private final long ttl;

    /**
     * Ctor.
     * @param pkt Project
     * @param lcks Locks
     * @param ttl Lock time
     */
    SyncProject(final Project pkt, final Locks lcks, final long ttl) {
        this.origin = pkt;
        this.locks = lcks;
        this.ttl = ttl;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file, final Project.Access mode)
        throws IOException {
        final long start = System.currentTimeMillis();
        final ReadWriteLock rwlock = this.locks.lock(this, file);
        final Lock lock;
        if (mode == Project.Access.READ) {
            lock = rwlock.readLock();
        } else {
            lock = rwlock.writeLock();
        }
        final Item item;
        try {
            // @checkstyle MagicNumber (1 line)
            if (!lock.tryLock(2L, TimeUnit.MINUTES)) {
                throw new IllegalStateException(
                    Logger.format(
                        "Failed to acquire \"%s\" in \"%s\" in %[ms]s: %s",
                        file, this.origin.pid(),
                        System.currentTimeMillis() - start,
                        lock
                    )
                );
            }
            final Cancellation cancellation = new Cancellation(lock);
            item = new SyncItem(this.origin.acq(file), cancellation);
            cancellation.schedule(this.ttl);
        } catch (final InterruptedException iex) {
            if (lock.tryLock()) {
                lock.unlock();
            }
            throw new IllegalStateException(
                String.format(
                    "Acquire interrupted; item=%s/%s(%s)",
                    this.pid(), file, mode
                ),
                iex
            );
        }
        return item;
    }
}
