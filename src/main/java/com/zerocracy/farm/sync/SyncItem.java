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

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.jstk.Item;
import io.sentry.Sentry;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;

/**
 * Synchronized and thread safe item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@EqualsAndHashCode(of = "origin")
final class SyncItem implements Item, Comparable<SyncItem> {

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * Semaphore.
     */
    private final Semaphore semaphore;

    /**
     * Item acquire statistics.
     */
    private final AtomicInteger statistics;

    /**
     * This timer will send error message with stacktrace to
     * Sentry if item acquired more than 20 seconds.
     */
    private final Timer timer;

    /**
     * Timer tasks.
     */
    private final List<TimerTask> tasks;

    /**
     * Ctor.
     * @param item Original item
     */
    SyncItem(final Item item) {
        this.origin = item;
        this.semaphore = new Semaphore(1, true);
        this.statistics = new AtomicInteger();
        this.timer = new Timer();
        this.tasks = new ArrayList<>(1);
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        synchronized (this.origin) {
            return this.origin.path();
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this.origin) {
            this.origin.close();
            this.semaphore.release();
        }
        synchronized (this.tasks) {
            for (final TimerTask task : this.tasks) {
                task.cancel();
            }
            this.tasks.clear();
        }
    }

    /**
     * Acquire access.
     * @throws InterruptedException If fails
     */
    public void acquire() throws InterruptedException {
        final long start = System.currentTimeMillis();
        if (!this.semaphore.tryAcquire((long) Tv.TWENTY, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                Logger.format(
                    "Failed to acquire \"%s\" in %[ms]s",
                    this.origin,
                    System.currentTimeMillis() - start
                )
            );
        }
        this.statistics.incrementAndGet();
        final SyncItem.ReportTask task = new SyncItem.ReportTask(
            new Exception(
                String.format(
                    "Item \"%s\" was opened for too long",
                    this.toString()
                )
            ).fillInStackTrace(),
            this.origin.toString()
        );
        synchronized (this.tasks) {
            this.tasks.add(task);
            this.timer.schedule(
                task,
                TimeUnit.MILLISECONDS.convert(Tv.TWENTY, TimeUnit.SECONDS)
            );
        }
    }

    @Override
    public int compareTo(final SyncItem other) {
        return this.statistics.get() - other.statistics.get();
    }

    /**
     * Timer task that send message with stacktrace to sentry.
     */
    private static final class ReportTask extends TimerTask {

        /**
         * Throwable with stacktrace.
         */
        private final Throwable stk;

        /**
         * Item name.
         */
        private final String item;

        /**
         * True if cancelled.
         */
        private final AtomicBoolean canceled;

        /**
         * Ctor.
         * @param stk Stacktrace container.
         * @param item Item name.
         */
        private ReportTask(final Throwable stk, final String item) {
            super();
            this.stk = stk;
            this.item = item;
            this.canceled = new AtomicBoolean(false);
        }

        @Override
        public void run() {
            if (!this.canceled.get()) {
                this.stk.printStackTrace();
                Sentry.capture(
                    new EventBuilder()
                        .withMessage(
                            String.format(
                                "Item `%s` kept open more than 20 seconds",
                                this.item
                            )
                        ).withSentryInterface(new ExceptionInterface(this.stk))
                );
            }
        }

        @Override
        public boolean cancel() {
            this.canceled.set(true);
            return super.cancel();
        }
    }
}
