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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * {@link Lock} using MongoDB.
 *
 * @since 1.0
 * @todo #1465:30min Implement external locking mechanism based in MongoDB.
 *  The lock must be set to some resource in a table where resource path and
 *  name must be set, along with time of lock. Then remove expects from
 *  MongoDBLockTest.
 */
public final class MongoDbLock implements Lock {

    @Override
    public StackTraceElement[] stacktrace() {
        throw new UnsupportedOperationException(
            "StackTraceElement[] is not implemented"
        );
    }

    @Override
    public void lock() {
        throw new UnsupportedOperationException(
            "lock() is not implemented"
        );
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException(
            "lockInterruptibly() is not implemented"
        );
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException(
            "tryLock() is not implemented"
        );
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit)
        throws InterruptedException {
        throw new UnsupportedOperationException(
            "tryLock(long time, TimeUnit unit) is not implemented"
        );
    }

    @Override
    public void unlock() {
        throw new UnsupportedOperationException(
            "unlock() is not implemented"
        );
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException(
            "newCondition() is not implemented"
        );
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException(
            "toString() is not implemented"
        );
    }
}
