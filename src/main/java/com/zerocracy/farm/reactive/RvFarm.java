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
package com.zerocracy.farm.reactive;

import com.jcabi.log.VerboseThreads;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.farm.fake.FkItem;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.Mapped;

/**
 * Reactive farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "origin")
public final class RvFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Brigade.
     */
    private final Brigade brigade;

    /**
     * Locks per projects.
     */
    private final Map<Project, Lock> locks;

    /**
     * Executor of flushes.
     */
    private final ExecutorService service;

    /**
     * How many flushes are in the line now?
     */
    private final AtomicInteger alive;

    /**
     * Ctor.
     * @param farm Original farm
     * @param bgd Stakeholders
     */
    public RvFarm(final Farm farm, final Brigade bgd) {
        this(farm, bgd, Runtime.getRuntime().availableProcessors() << 2);
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param bgd Stakeholders
     * @param threads How many threads to use
     */
    public RvFarm(final Farm farm, final Brigade bgd, final int threads) {
        this.origin = farm;
        this.brigade = bgd;
        this.service = Executors.newFixedThreadPool(
            threads, new VerboseThreads(RvFarm.class)
        );
        this.alive = new AtomicInteger();
        this.locks = new ConcurrentHashMap<>(0);
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        final Iterable<Project> list;
        if (query.equals(RvFarm.class.getCanonicalName())) {
            list = new IterableOf<>(
                (Project) file -> new FkItem(
                    Integer.toString(this.alive.get())
                )
            );
        } else {
            list = new Mapped<>(
                pkt -> new RvProject(
                    pkt,
                    () -> {
                        this.alive.incrementAndGet();
                        this.service.submit(
                            () -> {
                                final Lock lock = this.locks.computeIfAbsent(
                                    pkt, p -> new ReentrantLock()
                                );
                                lock.lock();
                                try {
                                    new Flush(pkt, this.brigade).flush();
                                    this.alive.decrementAndGet();
                                    return null;
                                } finally {
                                    lock.unlock();
                                }
                            }
                        );
                    }
                ),
                this.origin.find(query)
            );
        }
        return list;
    }

    @Override
    public void close() throws IOException {
        this.service.shutdown();
        try {
            if (!this.service.awaitTermination(1L, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Can't terminate service");
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } finally {
            this.origin.close();
        }
    }
}
