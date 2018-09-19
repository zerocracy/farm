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

import com.zerocracy.farm.props.PropsFarm;
import java.util.concurrent.locks.Lock;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.IsEmptyString;
import org.junit.Test;

/**
 * Test case for {@link MongoDbLock}.
 * @since 1.0
 * @todo #1644:30min Implement external locking mechanism based in MongoDB.
 *  Implement tests for methods in MongoDbLock (stacktrace(), lock(),
 *  lockInterruptibly(), tryLock(),
 *  tryLock(final long time, final TimeUnit unit), unlock() and newCondition()).
 *  These methods must behave as specified in in java 8
 *  java.util.concurrent.locks.Lock interface, except stacktrace() which must
 *  behave like SmartLock.stacktrace(). Then implement these methods so they'll
 *  pass all the tests below.
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class MongoDbLockTest {

    @Test
    public void buildsToStringWhenFresh() throws Exception {
        final Lock lock = new MongoDbLock(new PropsFarm(), "unlockedresource");
        MatcherAssert.assertThat(
            lock.toString(),
            new IsNot<>(
                new IsEmptyString()
            )
        );
    }

    @Test
    public void buildsToStringWhenLocked() throws Exception {
        final Lock lock = new MongoDbLock(new PropsFarm(), "lockedresource");
        lock.lock();
        try {
            MatcherAssert.assertThat(
                lock.toString(),
                new IsNot<>(
                    new IsEmptyString()
                )
            );
        } finally {
            lock.unlock();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void buildsToStringWhenLockedInterruptibly() throws Exception {
        final Lock lock = new MongoDbLock(new PropsFarm(), "someresource");
        lock.lockInterruptibly();
        try {
            MatcherAssert.assertThat(
                lock.toString(),
                new IsNot<>(
                    new IsEmptyString()
                )
            );
        } finally {
            lock.unlock();
        }
    }
}
