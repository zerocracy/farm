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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequestEntry;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimsQueueUrl;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.shutdown.ShutdownHook;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.Proc;
import org.cactoos.iterable.Partitioned;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.SolidScalar;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.UncheckedText;

/**
 * Monitor processing of message in SQS queue.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
public final class MessageMonitorProc implements Proc<Message> {

    /**
     * Maximum batch size of Amazon SQS change message visibility request.
     * @see https://docs.aws.amazon.com/AWSSimpleQueueService/latest/APIReference/API_ChangeMessageVisibilityBatch.html
     */
    public static final int VIS_BATCH_MAX = 10;

    /**
     * Origin.
     */
    private final Proc<Message> origin;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Monitor thread.
     */
    private final ExecutorService routine;

    /**
     * Messages to queue.
     */
    private final Set<Message> messages;

    /**
     * Duration of message validity.
     */
    private final int duration;

    /**
     * Shutdown hook.
     */
    private final ShutdownHook shutdown;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param origin Origin
     * @param shutdown Shutdown hook
     */
    public MessageMonitorProc(final Farm farm, final Proc<Message> origin,
        final ShutdownHook shutdown) {
        this(
            origin,
            farm,
            (long) Tv.SIXTY,
            (int) Duration.ofMinutes(2L).getSeconds(),
            shutdown
        );
    }

    /**
     * Ctor.
     *
     * @param origin Origin
     * @param farm Farm
     * @param interval Refresh interval
     * @param duration Duration of extension
     * @param shutdown Shutdown
     * @checkstyle ParameterNumber (4 lines)
     */
    public MessageMonitorProc(final Proc<Message> origin,
        final Farm farm,
        final long interval, final int duration,
        final ShutdownHook shutdown) {
        this.origin = origin;
        this.farm = farm;
        this.duration = duration;
        this.shutdown = shutdown;
        this.messages = ConcurrentHashMap.newKeySet();
        this.routine = new UncheckedScalar<>(
            new SolidScalar<>(
                () -> {
                    final ScheduledExecutorService svc =
                        Executors.newSingleThreadScheduledExecutor(
                            new VerboseThreads(MessageMonitorProc.class)
                        );
                    svc.scheduleWithFixedDelay(
                        new VerboseRunnable(
                            this::refreshMessageValidity, true, true
                        ),
                        interval, interval, TimeUnit.SECONDS
                    );
                    return svc;
                }
            )
        ).value();
    }

    @Override
    public void exec(final Message input) throws Exception {
        synchronized (this.messages) {
            this.messages.add(input);
        }
        try {
            final List<Message> btch = Collections.singletonList(input);
            if (this.sendMessageVisibilityBatch(btch)) {
                this.origin.exec(input);
            }
        } finally {
            synchronized (this.messages) {
                this.messages.remove(input);
            }
            new ExtSqs(this.farm).value().deleteMessage(
                new DeleteMessageRequest()
                    .withQueueUrl(new ClaimsQueueUrl(this.farm).asString())
                    .withReceiptHandle(input.getReceiptHandle())
            );
            Logger.info(
                this, "message %s was deleted", input.getMessageId()
            );
        }
    }

    /**
     * Refresh message validity.
     */
    private void refreshMessageValidity() {
        if (this.shutdown.check()) {
            final List<List<Message>> parts;
            synchronized (this.messages) {
                parts = new ArrayList<>(
                    new ListOf<>(
                        new Partitioned<>(
                            MessageMonitorProc.VIS_BATCH_MAX,
                            this.messages
                        )
                    )
                );
            }
            for (final List<Message> msgs : parts) {
                this.sendMessageVisibilityBatch(msgs);
            }
        } else {
            this.routine.shutdown();
        }
    }

    /**
     * Send change message visibility timeout batch request.
     * @return True if all processed
     * @param msgs Messages
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private boolean sendMessageVisibilityBatch(final List<Message> msgs) {
        final List<ChangeMessageVisibilityBatchRequestEntry> entries =
            new ArrayList<>(msgs.size());
        int num = 0;
        final AmazonSQS sqs = new UncheckedScalar<>(new ExtSqs(this.farm))
            .value();
        final String queue = new UncheckedText(new ClaimsQueueUrl(this.farm))
            .asString();
        Logger.debug(
            this,
            "changing visibility for %d messages to %d sec",
            this.messages.size(), this.duration
        );
        for (final Message msg : msgs) {
            synchronized (this.messages) {
                if (!this.messages.contains(msg)) {
                    continue;
                }
            }
            entries.add(
                new ChangeMessageVisibilityBatchRequestEntry(
                    String.format("msg_%d", num),
                    msg.getReceiptHandle()
                ).withVisibilityTimeout(this.duration)
            );
            num += 1;
        }
        final ChangeMessageVisibilityBatchResult res =
            sqs.changeMessageVisibilityBatch(queue, entries);
        Logger.debug(
            this,
            "successfully changed visibility for %d messages",
            res.getSuccessful().size()
        );
        res.getFailed().forEach(
            entry -> Logger.warn(
                this,
                "failed to change visibility for %s (%s - %s)",
                entry.getId(), entry.getCode(), entry.getMessage()
            )
        );
        return res.getFailed().isEmpty();
    }
}
