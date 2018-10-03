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
package com.zerocracy.farm.sync;

import com.zerocracy.Project;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.cactoos.func.SolidBiFunc;
import org.cactoos.func.UncheckedBiFunc;
import org.cactoos.scalar.UncheckedScalar;

/**
 * Lock for tests.
 *
 * @since 1.0
 */
final class TestLock implements Lock {

    /**
     * Lock func.
     */
    private static final UncheckedBiFunc<Project, String, Lock> LOCKS =
        new UncheckedBiFunc<>(
            new SolidBiFunc<>((pkt, res) -> new ReentrantLock())
        );

    /**
     * Lock.
     */
    private final UncheckedScalar<Lock> lck;

    /**
     * Ctor.
     *
     * @param project Project
     * @param resource resource
     */
    TestLock(final Project project, final String resource) {
        this.lck = new UncheckedScalar<>(
            () -> TestLock.LOCKS.apply(project, resource)
        );
    }

    @Override
    public void lock() {
        this.lck.value().lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lck.value().lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return this.lck.value().tryLock();
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit)
        throws InterruptedException {
        return this.lck.value().tryLock(time, unit);
    }

    @Override
    public void unlock() {
        this.lck.value().unlock();
    }

    @Override
    public Condition newCondition() {
        return this.lck.value().newCondition();
    }
}
