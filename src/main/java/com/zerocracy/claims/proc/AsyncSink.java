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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseCallable;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.claims.ClaimGuts;
import com.zerocracy.shutdown.ShutdownFarm;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Proc;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;

/**
 * Proc to execute origin proc asynchronously.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class AsyncSink {

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
     * Claim guts.
     */
    private final ClaimGuts guts;

    /**
     * Ctor.
     *
     * @param origin Origin proc
     * @param cgts Guts
     * @param shutdown Shutdown hook
     */
    public AsyncSink(final Proc<Message> origin,
        final ClaimGuts cgts, final ShutdownFarm.Hook shutdown) {
        this(
            Runtime.getRuntime().availableProcessors(),
            origin, cgts, shutdown
        );
    }

    /**
     * Ctor.
     *
     * @param threads Threads
     * @param origin Origin proc
     * @param cgts Claim guts
     * @param shutdown Shutdown hook
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    public AsyncSink(final int threads, final Proc<Message> origin,
        final ClaimGuts cgts, final ShutdownFarm.Hook shutdown) {
        this.service = Executors.newScheduledThreadPool(
            threads, new VerboseThreads(AsyncSink.class)
        );
        this.origin = origin;
        this.guts = cgts;
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

    /**
     * Monitor a queue.
     * @param queue Queue to monitor
     */
    public void monitor(final BlockingQueue<Message> queue) {
        try {
            this.service.submit(
                new VerboseCallable<>(
                    () -> {
                        final Thread thread = Thread.currentThread();
                        Logger.info(
                            this,
                            "Started async routine on thread %d:%s",
                            thread.getId(),
                            thread.getName()
                        );
                        while (!thread.isInterrupted()) {
                            try {
                                this.exec(queue.take());
                            } catch (final IOException exx) {
                                Logger.error(
                                    this,
                                    "Failed to process message: %[exception]s",
                                    exx
                                );
                            } catch (final InterruptedException err) {
                                thread.interrupt();
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
    }

    /**
     * Exec a message.
     * @param msg Message
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void exec(final Message msg) throws IOException {
        final List<Message> input =
            Collections.singletonList(msg);
        try {
            this.guts.start(input);
            Logger.info(
                this, "Processing a messages",
                input.size()
            );
            new IoCheckedScalar<>(new And(this.origin, input)).value();
        } finally {
            if (this.count.decrementAndGet() == 0
                && this.shutdown.stopping()) {
                this.shutdown.complete();
                Thread.currentThread().interrupt();
            }
            this.guts.stop(input);
        }
    }
}
