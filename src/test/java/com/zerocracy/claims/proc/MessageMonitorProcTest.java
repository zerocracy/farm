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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.zerocracy.shutdown.ShutdownFarm;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Test case for {@link MessageMonitorProc}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class MessageMonitorProcTest {

    @Test
    public void extendsMesssageValidity() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Message msg = Mockito.mock(Message.class);
        Mockito.when(msg.getReceiptHandle()).thenAnswer(
            invocation -> {
                latch.countDown();
                return "blah";
            }
        );
        final AmazonSQS sqs = Mockito.mock(AmazonSQS.class);
        final ShutdownFarm.Hook hook = new ShutdownFarm.Hook();
        final String queue = "test-queue";
        try {
            new MessageMonitorProc(
                input -> latch.await(1, TimeUnit.MINUTES),
                () -> sqs,
                queue,
                1,
                1,
                hook
            ).exec(msg);
            Mockito.verify(
                sqs,
                Mockito.timeout(Duration.ofMinutes(1L).toMillis())
                    .atLeastOnce()
            )
                .changeMessageVisibilityBatch(
                    ArgumentMatchers.eq(queue), ArgumentMatchers.anyList()
                );
        } finally {
            hook.complete();
        }
    }
}
