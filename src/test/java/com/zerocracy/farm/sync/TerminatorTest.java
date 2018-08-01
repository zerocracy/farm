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

import com.zerocracy.farm.fake.FkProject;
import java.util.concurrent.locks.Lock;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Terminator}.
 * @since 1.0
 */
public final class TerminatorTest {
    /**
     * Tests that {@link Terminator} implementation, when the
     * {@link Lock#tryLock()} returns {@code false}, executes again, and calls
     * {@link Lock#unlock()} only when it acquired the lock by itself.
     * @throws Exception if error occurred during test.
     */
    @Test
    public void runsAgainIfTryLockFail() throws Exception {
        final Lock lock = Mockito.mock(Lock.class);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any()))
            .thenReturn(false).thenReturn(true);
        final FkProject project = new FkProject();
        try (final Terminator terminator = new Terminator(1)) {
            new Thread(
                () -> {
                    terminator.submit(project, "foo", lock);
                }
            ).start();
            // @checkstyle MagicNumber (3 lines)
            Mockito.verify(lock, Mockito.timeout(10000).times(2))
                .tryLock(Mockito.anyLong(), Mockito.any());
            Mockito.verify(lock, Mockito.timeout(10000).times(1)).unlock();
        }
    }
}
