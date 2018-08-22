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

import java.util.concurrent.locks.Lock;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.IsEmptyString;
import org.junit.Test;

/**
 * Test case for {@link MongoDbLock}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class MongoDbLockTest {

    @Test(expected = UnsupportedOperationException.class)
    public void buildsToStringWhenFresh() throws Exception {
        final Lock lock = new MongoDbLock();
        MatcherAssert.assertThat(
            lock.toString(),
            new IsNot<>(
                new IsEmptyString()
            )
        );
    }

    @Test(expected = UnsupportedOperationException.class)
    public void buildsToStringWhenLocked() throws Exception {
        final Lock lock = new MongoDbLock();
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
        final Lock lock = new MongoDbLock();
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
