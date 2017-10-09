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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
public final class SyncProject implements Project {

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
     */
    public SyncProject(final Project pkt) {
        this(pkt, new ReentrantLock());
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param lck Lock
     */
    public SyncProject(final Project pkt, final Lock lck) {
        this.origin = pkt;
        this.lock = lck;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final long start = System.currentTimeMillis();
        try {
            // @checkstyle MagicNumber (1 line)
            if (!this.lock.tryLock(5L, TimeUnit.MINUTES)) {
                throw new IllegalStateException(
                    Logger.format(
                        "Failed to acquire \"%s\" in \"%s\" in %[ms]s",
                        file, this.origin,
                        System.currentTimeMillis() - start
                    )
                );
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        return new SyncItem(this.origin.acq(file), this.lock);
    }
}
