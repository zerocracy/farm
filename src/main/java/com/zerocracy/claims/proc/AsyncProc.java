/*
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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseCallable;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.shutdown.ShutdownFarm;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Proc;
import org.cactoos.scalar.And;

/**
 * Proc to execute origin proc asynchronously.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class AsyncProc implements Proc<List<Message>> {

    /**
     * Executor.
     */
    private final ScheduledExecutorService service;

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Shutdown hook.
     */
    private final ShutdownFarm.Hook shutdown;

    /**
     * Counter.
     */
    private final AtomicInteger count;

    /**
     * Ctor.
     *
     * @param origin Origin proc
     * @param shutdown Shutdown hook
     */
    public AsyncProc(final Proc<Message> origin,
        final ShutdownFarm.Hook shutdown) {
        this(Runtime.getRuntime().availableProcessors(), origin, shutdown);
    }

    /**
     * Ctor.
     *
     * @param threads Threads
     * @param origin Origin proc
     * @param shutdown Shutdown hook
     */
    public AsyncProc(final int threads, final Proc<Message> origin,
        final ShutdownFarm.Hook shutdown) {
        this.service = Executors.newScheduledThreadPool(
            threads, new VerboseThreads(AsyncProc.class)
        );
        this.origin = origin;
        this.shutdown = shutdown;
        this.count = new AtomicInteger();
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                () -> {
                    if (!this.shutdown.check()) {
                        this.service.shutdown();
                        try {
                            this.service.awaitTermination(
                                Tv.FIVE,
                                TimeUnit.MINUTES
                            );
                        } catch (final InterruptedException err) {
                            Logger.info(
                                this,
                                "Service wait was interrupted"
                            );
                        }
                        Logger.info(
                            this,
                            "Shutting down with %d tasks still executing",
                            this.service.shutdownNow().size()
                        );
                    }
                }
            ),
            1, 1, TimeUnit.MINUTES
        );
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void exec(final List<Message> input) {
        final int cnt = this.count.incrementAndGet();
        try {
            this.service.submit(
                new VerboseCallable<>(
                    () -> {
                        try {
                            Logger.info(
                                this, "Processing %d messages",
                                input.size()
                            );
                            new And(this.origin, input).value();
                        } finally {
                            if (this.count.decrementAndGet() == 0
                                && this.shutdown.stopping()) {
                                this.shutdown.complete();
                            }
                        }
                        return null;
                    },
                    true, true
                )
            );
        } catch (final RejectedExecutionException err) {
            this.count.decrementAndGet();
            throw new IllegalStateException("Task was rejected", err);
        }
        Logger.info(
            this, "Submitted %d messages (count=%d)",
            input.size(), cnt
        );
    }
}
