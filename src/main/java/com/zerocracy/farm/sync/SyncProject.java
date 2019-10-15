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
     * Terminator.
     */
    private final Terminator terminator;

    /**
     * Ctor.
     * @param pkt Project
     * @param lcks Locks
     * @param tmr Terminator
     */
    SyncProject(final Project pkt, final Locks lcks, final Terminator tmr) {
        this.origin = pkt;
        this.locks = lcks;
        this.terminator = tmr;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final long start = System.currentTimeMillis();
        final Lock lock = this.locks.lock(this, file);
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
        this.terminator.submit(this, file, lock);
        return new WarnItem(
            String.format("%s/%s", this.pid(), file),
            new SyncItem(this.origin.acq(file), lock),
            System.nanoTime()
        );
    }
}
