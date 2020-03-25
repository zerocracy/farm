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
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import lombok.EqualsAndHashCode;
import org.cactoos.Func;
import org.cactoos.Proc;

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
    private final ReadWriteLock lock;

    /**
     * Ctor.
     * @param item Original item
     * @param lck Lock
     */
    SyncItem(final Item item, final ReadWriteLock lck) {
        this.origin = item;
        this.lock = lck;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public <T> T read(final Func<Path, T> reader) throws IOException {
        final Lock lck = this.lock.readLock();
        Logger.debug(this, "#read(): try %s", lck);
        this.tryLock(lck);
        Logger.debug(this, "#read(): acq %s", lck);
        try {
            return this.origin.read(reader);
        } finally {
            lck.unlock();
            Logger.debug(this, "#read(): unlock %s", lck);
        }
    }

    @Override
    public void update(final Proc<Path> writer) throws IOException {
        final Lock lck = this.lock.writeLock();
        Logger.debug(this, "#update(): try %s", lck);
        this.tryLock(lck);
        Logger.debug(this, "#update(): acq %s", lck);
        try {
            this.origin.update(writer);
        } finally {
            lck.unlock();
            Logger.debug(this, "#update(): unlocked %s", lck);
        }
    }

    /**
     * Try to lock the resource with given time.
     * @param lck Lock to acquire
     */
    private void tryLock(final Lock lck) {
        final long start = System.currentTimeMillis();
        try {
            // @checkstyle MagicNumber (1 line)
            if (!lck.tryLock(15L, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                    Logger.format(
                        "Failed to acquire a lock %s/%s for \"%s\" in %[ms]s",
                        this.lock, lck,
                        this.origin,
                        System.currentTimeMillis() - start
                    )
                );
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                Logger.format(
                    "%s interrupted while waiting for \"%s\" in %[ms]s",
                    Thread.currentThread().getName(),
                    this.origin,
                    System.currentTimeMillis() - start
                ),
                ex
            );
        }
    }
}
