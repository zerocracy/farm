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
package com.zerocracy.farm;

import com.jcabi.log.Logger;
import com.zerocracy.farm.sync.Lock;
import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import lombok.EqualsAndHashCode;

/**
 * Lock that is smart.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
public final class SmartLock implements Lock {

    /**
     * ID.
     */
    private final String uid = UUID.randomUUID().toString().substring(24);

    /**
     * Original lock.
     */
    private final ReentrantLock origin = new ReentrantLock();

    /**
     * Start.
     */
    private final AtomicLong start = new AtomicLong();

    /**
     * Owner.
     */
    private final AtomicReference<WeakReference<Thread>> owner =
        new AtomicReference<>();

    @Override
    public StackTraceElement[] stacktrace() {
        final Thread thread;
        final WeakReference<Thread> ref = this.owner.get();
        if (ref == null) {
            thread = null;
        } else {
            thread = ref.get();
        }
        final StackTraceElement[] array;
        if (thread == null) {
            array = new StackTraceElement[0];
        } else {
            array = thread.getStackTrace();
        }
        return array;
    }

    @Override
    public String toString() {
        final String text;
        if (this.origin.isLocked()) {
            final Thread thread = this.owner.get().get();
            final String name;
            if (thread == null) {
                name = "<disposed>";
            } else {
                name = thread.getName();
            }
            text = Logger.format(
                "%s/%[ms]s/%d/%d/%b/%b by %s",
                this.uid,
                System.currentTimeMillis() - this.start.get(),
                this.origin.getHoldCount(),
                this.origin.getQueueLength(),
                this.origin.hasQueuedThreads(),
                this.origin.isHeldByCurrentThread(),
                name
            );
        } else {
            text = "free";
        }
        return text;
    }

    @Override
    public void lock() {
        this.origin.lock();
        this.start.set(System.currentTimeMillis());
        this.owner.set(new WeakReference<>(Thread.currentThread()));
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.origin.lockInterruptibly();
        this.start.set(System.currentTimeMillis());
        this.owner.set(new WeakReference<>(Thread.currentThread()));
    }

    @Override
    public boolean tryLock() {
        final boolean done = this.origin.tryLock();
        if (done) {
            this.start.set(System.currentTimeMillis());
            this.owner.set(new WeakReference<>(Thread.currentThread()));
        }
        return done;
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit)
        throws InterruptedException {
        final boolean done = this.origin.tryLock(time, unit);
        if (done) {
            this.start.set(System.currentTimeMillis());
            this.owner.set(new WeakReference<>(Thread.currentThread()));
        }
        return done;
    }

    @Override
    public void unlock() {
        this.origin.unlock();
        final WeakReference<Thread> ref = this.owner.get();
        if (ref != null) {
            ref.clear();
        }
    }

    @Override
    public Condition newCondition() {
        return this.origin.newCondition();
    }
}
