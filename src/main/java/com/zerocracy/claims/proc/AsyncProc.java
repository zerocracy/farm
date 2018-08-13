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
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequest;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseCallable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimsQueueUrl;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.shutdown.ShutdownFarm;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Partitioned;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.UncheckedText;

/**
 * Proc to execute origin proc asynchronously.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
public final class AsyncProc implements Proc<List<Message>> {
    /**
     * Maximum size of visibility timeout batch request.
     * @see https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_ChangeMessageVisibilityBatch.html
     */
    private static final int VIS_BATCH_MAX = 10;
    /**
     * Executor.
     */
    private final ExecutorService service;

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Shutdown hook.
     */
    private final ShutdownFarm.Hook shutdown;

    /**
     * Counter.
     */
    private final AtomicInteger count;

    /**
     * Amazon SQS instance.
     */
    private final UncheckedScalar<AmazonSQS> sqs;

    /**
     * Message queue name.
     */
    private final String queue;

    /**
     * Ctor.
     *
     * @param threads Threads
     * @param farm Farm
     * @param origin Origin proc
     * @param shutdown Shutdown hook
     * @checkstyle ParameterNumber (4 lines)
     */
    public AsyncProc(final Farm farm, final int threads,
        final Proc<Message> origin,
        final ShutdownFarm.Hook shutdown) {
        this(
            threads,
            origin,
            new ExtSqs(farm),
            new UncheckedText(new ClaimsQueueUrl(farm)).asString(),
            shutdown
        );
    }

    /**
     * Ctor.
     *
     * @param threads Threads
     * @param origin Origin proc
     * @param sqs SQS client
     * @param queue Name of SQS queue
     * @param shutdown Shutdown hook
     * @checkstyle ParameterNumber (4 lines)
     */
    public AsyncProc(final int threads, final Proc<Message> origin,
        final Scalar<AmazonSQS> sqs, final String queue,
        final ShutdownFarm.Hook shutdown) {
        this.service = Executors.newFixedThreadPool(
            threads, new VerboseThreads(AsyncProc.class)
        );
        this.origin = origin;
        this.shutdown = shutdown;
        this.sqs = new UncheckedScalar<>(sqs);
        this.queue = queue;
        this.count = new AtomicInteger();
    }

    // @todo #1567:30min This method is too complicated at the moment. Let's
    //  separate the functionality of extending validity of messages into
    //  another class somehow. Note, one complication: we need to keep track
    //  of which messages have already been sent by AsyncProc for processing
    //  (since they are deleted from the queue very soon after). After
    //  refactoring let's remove any unnecessary suppressions here.
    // @checkstyle ExecutableStatementCount (60 lines)
    @Override
    @SuppressWarnings(
        {
            "PMD.PrematureDeclaration", "PMD.AvoidInstantiatingObjectsInLoops"
        }
    )
    public void exec(final List<Message> input) {
        final int cnt = this.count.incrementAndGet();
        try {
            final List<Message> copy = new CopyOnWriteArrayList<>(input);
            final CountDownLatch done = new CountDownLatch(copy.size());
            this.service.submit(
                new VerboseCallable<>(
                    () -> {
                        try {
                            Logger.info(
                                this, "Processing %d messages",
                                input.size()
                            );
                            final Iterator<Message> messages = copy.iterator();
                            while (messages.hasNext()) {
                                this.origin.exec(messages.next());
                                messages.remove();
                                done.countDown();
                            }
                        } finally {
                            if (this.count.decrementAndGet() == 0
                                && this.shutdown.stopping()) {
                                this.shutdown.complete();
                            }
                        }
                        return null;
                    },
                    true, true
                )
            );
            do {
                for (
                    final Iterable<Message> msgs
                        : new Partitioned<>(
                            AsyncProc.VIS_BATCH_MAX, copy
                        )
                ) {
                    this.extendMessage(msgs);
                }
            } while (done.await(1, TimeUnit.MINUTES));
        } catch (final RejectedExecutionException err) {
            this.count.decrementAndGet();
            throw new IllegalStateException("Task was rejected", err);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            Logger.warn(
                this,
                "Interrupted while processing messages: %[exception]s",
                ex
            );
        }
        Logger.info(
            this, "Submitted %d messages (count=%d)",
            input.size(), cnt
        );
    }

    /**
     * Extend message visibility.
     * @param msgs Messages
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void extendMessage(final Iterable<Message> msgs) {
        final List<ChangeMessageVisibilityBatchRequestEntry> entries =
            new ArrayList<>(new LengthOf(msgs).intValue());
        int num = 0;
        for (final Message msg : msgs) {
            entries.add(
                new ChangeMessageVisibilityBatchRequestEntry(
                    String.format("msg_%d", num),
                    msg.getReceiptHandle()
                ).withVisibilityTimeout(
                    (int) Duration.ofMinutes(2).getSeconds()
                )
            );
            num += 1;
        }
        this.sqs.value().changeMessageVisibilityBatch(
            new ChangeMessageVisibilityBatchRequest(
                this.queue, entries
            )
        );
    }
}
