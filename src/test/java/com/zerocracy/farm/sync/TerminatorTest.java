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

import com.jcabi.aspects.Tv;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Terminator}.
 * @since 1.0
 */
public final class TerminatorTest {
    /**
     * Terminator can interrupt thread after lock timeout and checks lock again.
     * Tests that after calling
     * {@link Terminator#submit(Project, String, Lock)}, if the lock cannot be
     * acquired by the terminator thread, it will interrupt the thread and
     * repeat the lock check.
     * @throws Exception if error occurred during test.
     */
    @Test
    public void interruptsThreadAfterLockTimeoutAndChecksLockAgain()
        throws Exception {
        final Lock lock = Mockito.mock(Lock.class);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any()))
            .thenReturn(false).thenReturn(true);
        final FkProject project = new FkProject();
        final PropsFarm farm = new PropsFarm();
        try (final Terminator terminator = new Terminator(farm, 1L)) {
            final AtomicBoolean interrupted = new AtomicBoolean(false);
            final Thread thread = new Thread(
                () -> {
                    terminator.submit(project, "foo", lock);
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(Tv.FIVE));
                    } catch (final InterruptedException ex) {
                        interrupted.set(true);
                    }
                }
            );
            thread.start();
            thread.join();
            Mockito.verify(
                lock,
                Mockito.timeout(TimeUnit.SECONDS.toMillis(Tv.FIVE)).times(2)
            ).tryLock(Mockito.anyLong(), Mockito.any());
            Mockito.verify(
                lock,
                Mockito.timeout(TimeUnit.SECONDS.toMillis(Tv.FIVE)).times(1)
            ).unlock();
            MatcherAssert.assertThat(
                interrupted.get(), Matchers.is(true)
            );
        }
    }
}
