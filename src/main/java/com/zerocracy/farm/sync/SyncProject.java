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
import org.cactoos.func.And;
import org.cactoos.func.AsyncFunc;
import org.cactoos.func.False;
import org.cactoos.func.FuncAsProc;
import org.cactoos.func.Ternary;
import org.cactoos.func.UncheckedScalar;
import org.cactoos.list.LimitedIterable;
import org.cactoos.list.MappedIterable;
import org.cactoos.list.SortedIterable;

/**
 * Pool project.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
        this.clean = none -> new Ternary<>(
            () -> this.pool.size() > threshold,
            () -> {
                synchronized (this.pool) {
                    return new UncheckedScalar<>(
                        new And(
                            new LimitedIterable<>(
                                new SortedIterable<CmpEntry<String,
                                    SyncItem>>(
                                    new MappedIterable<>(
                                        this.pool.entrySet(),
                                        CmpEntry::new
                                    )
                                ),
                                this.pool.size() - this.threshold
                            ),
                            new FuncAsProc<>(
                                entry -> this.pool.remove(entry.getKey())
                            )
                        )
                    ).value();
                }
            },
            new False()
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
