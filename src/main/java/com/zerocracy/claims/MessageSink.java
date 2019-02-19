/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.claims;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.claims.proc.AsyncSink;
import com.zerocracy.claims.proc.BrigadeProc;
import com.zerocracy.claims.proc.CountingProc;
import com.zerocracy.claims.proc.ExpiryProc;
import com.zerocracy.claims.proc.FootprintProc;
import com.zerocracy.claims.proc.MessageMonitorProc;
import com.zerocracy.claims.proc.SentryProc;
import com.zerocracy.shutdown.ShutdownFarm;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Claims message sink.
 * <p>
 *     This object takes new claims from message queue,
 *     and process them.
 * </p>
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class MessageSink {

    /**
     * Async sink.
     */
    private final AsyncSink asynk;

    /**
     * Ctor.
     * @param farm Farm
     * @param shutdown Shutdown
     */
    public MessageSink(final Farm farm, final ShutdownFarm.Hook shutdown) {
        this.asynk = new AsyncSink(
            new MessageMonitorProc(
                farm,
                new ExpiryProc(
                    new SentryProc(
                        farm,
                        new FootprintProc(
                            farm,
                            new CountingProc(
                                new BrigadeProc(farm),
                                new AtomicInteger()
                            )
                        )
                    )
                ),
                shutdown
            ),
            shutdown
        );
    }

    /**
     * Start sink.
     * @param queue Message queue
     */
    public void start(final BlockingQueue<Message> queue) {
        final int processors = Runtime.getRuntime().availableProcessors();
        Logger.info(
            this,
            "Starting message sink with %d async routines",
            processors
        );
        final Thread thread = new Thread(
            () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final Message msg = queue.take();
                        final boolean important = MsgPriority.from(msg).value()
                            <= MsgPriority.NORMAL.value();
                        if (!important) {
                            while (this.asynk.tasks() >= Tv.TEN) {
                                TimeUnit.SECONDS.sleep(1L);
                            }
                        }
                        this.asynk.execAsync(msg);
                    } catch (final InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        );
        thread.setDaemon(true);
        thread.setName(this.getClass().getSimpleName());
        thread.start();
    }
}
