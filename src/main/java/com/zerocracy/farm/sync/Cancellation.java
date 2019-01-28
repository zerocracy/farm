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

import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import org.cactoos.Proc;
import org.cactoos.func.IoCheckedProc;

/**
 * Lock cancellation.
 *
 * @since 1.0
 */
public final class Cancellation implements Runnable {

    /**
     * Empty void proc.
     */
    private static final Proc<Void> VOID_PROC = none -> {
    };

    /**
     * Scheduled thread pool.
     */
    private static final ScheduledExecutorService EXEC =
        Executors.newScheduledThreadPool(Tv.EIGHT);

    /**
     * Lock.
     */
    private final Lock lock;

    /**
     * Cancelled flag.
     */
    private final AtomicBoolean cancelled;

    /**
     * Lock holder thread reference.
     */
    private final WeakReference<Thread> tref;

    /**
     * Scheduled task.
     */
    private final AtomicReference<ScheduledFuture<?>> task;

    /**
     * Ctor.
     * @param lock Lock
     */
    public Cancellation(final Lock lock) {
        this.lock = lock;
        this.cancelled = new AtomicBoolean();
        this.tref = new WeakReference<>(Thread.currentThread());
        this.task = new AtomicReference<>();
    }

    /**
     * Cancel current lock.
     * @param interrupt Interrupt lock holder thread?
     */
    public void cancel(final boolean interrupt) {
        try {
            this.cancel(
                interrupt, new IoCheckedProc<>(Cancellation.VOID_PROC)
            );
        } catch (final IOException err) {
            throw new IllegalStateException(err);
        }
    }

    /**
     * Cancel after action.
     * @param action Action to execute
     * @throws IOException If action fails
     */
    public void cancelAfter(final Proc<Void> action) throws IOException {
        this.cancel(false, new IoCheckedProc<>(action));
    }

    /**
     * Schedule cancellation with delay.
     * @param millis Delay
     */
    public void schedule(final long millis) {
        this.task.set(
            Cancellation.EXEC.schedule(this, millis, TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public void run() {
        this.cancel(true);
    }

    /**
     * Cancel internal implementation.
     * @param interrupt Interrupt holder thread
     * @param action Action to execute before cancel
     * @throws IOException If action fails
     */
    private void cancel(final boolean interrupt,
        final IoCheckedProc<Void> action)
        throws IOException {
        Cancellation.cancelTask(this.task);
        if (this.cancelled.compareAndSet(false, true)) {
            try {
                action.exec(null);
            } finally {
                if (interrupt) {
                    Cancellation.interrupt(this.tref);
                } else {
                    this.lock.unlock();
                }
            }
        }
    }

    /**
     * Cancel scheduled task.
     * @param task Task ref
     */
    private static void cancelTask(
        final AtomicReference<? extends Future<?>> task
    ) {
        final Future<?> future = task.getAndSet(null);
        if (future != null && future.isCancelled()) {
            future.cancel(false);
        }
    }

    /**
     * Interrupt lock holder thread and clear thread reference.
     * @param tref Holder thread reference
     */
    private static void interrupt(final WeakReference<Thread> tref) {
        final Thread thread = tref.get();
        tref.clear();
        if (thread != null) {
            thread.interrupt();
        }
    }
}
