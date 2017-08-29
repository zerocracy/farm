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

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.cactoos.Proc;
import org.cactoos.func.AsyncFunc;
import org.cactoos.func.ProcOf;
import org.cactoos.iterable.Limited;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sorted;
import org.cactoos.scalar.And;
import org.cactoos.scalar.False;
import org.cactoos.scalar.SyncScalar;
import org.cactoos.scalar.Ternary;

/**
 * Pool project.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.1
 */
@EqualsAndHashCode(of = "origin")
final class SyncProject implements Project {

    /**
     * Default max pool size.
     */
    private static final int DEFAULT_THRESHOLD = 50;

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Pool of items.
     */
    private final Map<String, SyncItem> pool;

    /**
     * Max pool size.
     */
    private final int threshold;

    /**
     * Clean action.
     */
    private final Proc<Void> clean;

    /**
     * Ctor.
     * @param pkt Project
     * @param map Pool of items
     */
    SyncProject(final Project pkt, final Map<String, SyncItem> map) {
        this(pkt, map, SyncProject.DEFAULT_THRESHOLD);
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param map Pool of items
     * @param threshold Max pool size
     */
    SyncProject(
        final Project pkt,
        final Map<String, SyncItem> map,
        final int threshold
    ) {
        this.origin = pkt;
        this.pool = map;
        this.threshold = threshold;
        this.clean = none -> new SyncScalar<>(
            new Ternary<>(
                () -> this.pool.size() > threshold,
                new And(
                    new Limited<>(
                        new Sorted<CmpEntry<String, SyncItem>>(
                            new Mapped<>(
                                this.pool.entrySet(),
                                CmpEntry::new
                            )
                        ),
                        this.pool.size() - this.threshold
                    ),
                    new ProcOf<>(e -> this.pool.remove(e.getKey()))
                ),
                new False()
            ),
            this.pool
        ).value();
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final String location = String.format(
            "%s %s", this.origin, file
        );
        synchronized (this.pool) {
            if (!this.pool.containsKey(location)) {
                this.pool.put(
                    location,
                    new SyncItem(this.origin.acq(file))
                );
            }
            final SyncItem item = this.pool.get(location);
            try {
                item.acquire();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            if (this.pool.size() > this.threshold) {
                new AsyncFunc<>(this.clean).apply(null);
            }
            return item;
        }
    }
}
