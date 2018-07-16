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

import com.zerocracy.RunsInThreads;
import java.util.concurrent.locks.Lock;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SmartLock}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SmartLockTest {

    @Test
    public void buildsToStringInMultiThreads() throws Exception {
        final Lock lock = new SmartLock();
        MatcherAssert.assertThat(
            inc -> {
                lock.lock();
                try {
                    return !lock.toString().isEmpty();
                } finally {
                    lock.unlock();
                }
            },
            new RunsInThreads<>(true)
        );
    }

    @Test
    public void buildsToStringWhenFresh() throws Exception {
        final Lock lock = new SmartLock();
        MatcherAssert.assertThat(
            lock.toString(),
            Matchers.not(Matchers.isEmptyString())
        );
    }

    @Test
    public void buildsToStringWhenLocked() throws Exception {
        final Lock lock = new SmartLock();
        lock.lock();
        try {
            MatcherAssert.assertThat(
                lock.toString(),
                Matchers.not(Matchers.isEmptyString())
            );
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void buildsToStringWhenLockedInterruptibly() throws Exception {
        final Lock lock = new SmartLock();
        lock.lockInterruptibly();
        try {
            MatcherAssert.assertThat(
                lock.toString(),
                Matchers.not(Matchers.isEmptyString())
            );
        } finally {
            lock.unlock();
        }
    }

}
