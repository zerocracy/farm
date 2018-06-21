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
package com.zerocracy.farm.reactive;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseCallable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Project;
import com.zerocracy.ShutUp;
import com.zerocracy.farm.SmartLock;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Reactive farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class AsyncFlush implements Flush {

    /**
     * Queue length (the shorter the faster we are, but we may
     * lose some claims).
     */
    private static final int QUEUE_LENGTH = 3;

    /**
     * Original flush.
     */
    private final Flush origin;

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
    private final Map<Project, AtomicInteger> alive;

    /**
     * Ctor.
     * @param flush Original flush
     */
    AsyncFlush(final Flush flush) {
        this(flush, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Ctor.
     * @param flush Original flush
     * @param threads Threads to use
     */
    AsyncFlush(final Flush flush, final int threads) {
        this.origin = flush;
        this.service = Executors.newFixedThreadPool(
            threads, new VerboseThreads(AsyncFlush.class)
        );
        this.alive = new ConcurrentHashMap<>(0);
        this.locks = new ConcurrentHashMap<>(0);
    }

    @Override
    public void exec(final Project project) {
        if (this.locks.size() > Tv.HUNDRED) {
            throw new IllegalStateException(
                String.format(
                    "%s pool overflow, too many locks: %d",
                    this.getClass().getCanonicalName(), this.locks.size()
                )
            );
        }
        final AtomicInteger total = this.alive.computeIfAbsent(
            project, p -> new AtomicInteger()
        );
        if (total.get() < AsyncFlush.QUEUE_LENGTH) {
            this.service.submit(
                new VerboseCallable<>(
                    () -> {
                        final Lock lock = this.locks.computeIfAbsent(
                            project, p -> new SmartLock()
                        );
                        try {
                            lock.lock();
                            try {
                                this.origin.exec(project);
                            } finally {
                                lock.unlock();
                            }
                        } finally {
                            total.decrementAndGet();
                        }
                        return null;
                    },
                    true, true
                )
            );
            total.incrementAndGet();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            new ShutUp(this.service).close();
        } finally {
            this.origin.close();
        }
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Iterable<Directive> value() throws Exception {
        return new Directives()
            .push()
            .append(this.origin.value())
            .pop()
            .add("alive")
            .append(
                new Joined<Directive>(
                    new Mapped<>(
                        ent -> new Directives().add("count")
                            .attr("pid", ent.getKey().pid())
                            .set(ent.getValue().toString()).up(),
                        this.alive.entrySet()
                    )
                )
            )
            .up()
            .add("locks")
            .append(
                new Joined<Directive>(
                    new Mapped<>(
                        ent -> new Directives().add("lock")
                            .attr("pid", ent.getKey().pid())
                            .set(ent.getValue().toString()).up(),
                        this.locks.entrySet()
                    )
                )
            )
            .up();
    }
}
