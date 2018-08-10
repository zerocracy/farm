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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link CountingProc}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class CountingProcTest {
    @Test
    public void goesToZeroAfterFinish() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        new CountingProc(msg -> { }, count).exec(Mockito.mock(Message.class));
        MatcherAssert.assertThat(
            "Counter should be 0 after exec finishes.",
            count.get(),
            new IsEqual<>(0)
        );
    }

    @Test
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    // @todo #1539:30min Create a FakeMessage and use it each test where
    //  a Message is used. An example is in staysAtOneIfBlocked, remove Mockito
    //  mock and use the newly created fake message.
    public void staysAtOneIfBlocked() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch block = new CountDownLatch(1);
        final CountDownLatch inside = new CountDownLatch(1);
        new Thread(
            () -> {
                try {
                    start.await();
                    new CountingProc(
                        msg -> {
                            inside.countDown();
                            block.await();
                        }, count
                    ).exec(Mockito.mock(Message.class));
                    // @checkstyle IllegalCatchCheck (1 line)
                } catch (final Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        ).start();
        MatcherAssert.assertThat(
            "Counter should be 0 before exec starts.",
            count.get(),
            new IsEqual<>(0)
        );
        start.countDown();
        inside.await();
        MatcherAssert.assertThat(
            "Counter should be 1 while exec is still running.",
            count.get(),
            new IsEqual<>(1)
        );
    }
}
