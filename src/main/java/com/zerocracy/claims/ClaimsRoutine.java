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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.claims.proc.MsgExpired;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.shutdown.ShutdownFarm;
import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.UncheckedText;

/**
 * Claims routine.
 * <p>
 * This class uses long-polling to fetch claims from SQS queue.
 *
 * @since 1.0
 * @todo #1731:30min ClaimsRoutine is too complex and not testable,
 *  let's refactor it to few simpler classes and unit test them
 *  if possible.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals",
        "PMD.AvoidInstantiatingObjectsInLoops",
        "PMD.ExcessiveImports"
    }
)
public final class ClaimsRoutine implements Runnable, Closeable {

    /**
     * Message by priority comparator.
     */
    private static final Comparator<Message> BY_PRIORITY =
        Comparator.comparingInt(msg -> MsgPriority.from(msg).value());

    /**
     * Until attribute.
     */
    private static final String UNTIL = "until";

    /**
     * Messages limit.
     */
    private static final int LIMIT = 8;

    /**
     * Max size of local message queue.
     */
    private static final int QUEUE_SIZE = 1024;

    /**
     * Delay to fetch claims.
     */
    private static final long DELAY = 12L;

    /**
     * Scheduled service.
     */
    private final ScheduledExecutorService service;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Local message queue.
     */
    private final BlockingQueue<Message> queue;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public ClaimsRoutine(final Farm farm) {
        this.service = Executors.newSingleThreadScheduledExecutor(
            new VerboseThreads(ClaimsRoutine.class)
        );
        this.farm = farm;
        this.queue = new PriorityBlockingQueue<>(
            ClaimsRoutine.QUEUE_SIZE, ClaimsRoutine.BY_PRIORITY
        );
    }

    /**
     * Start routine.
     *
     * @param shutdown Shutdown hook
     */
    public void start(final ShutdownFarm.Hook shutdown) {
        Logger.info(
            this,
            "Starting claims routine with local queue size = %s",
            ClaimsRoutine.QUEUE_SIZE
        );
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(
                new ClaimsRoutine.ShutdownRunnable(this, shutdown)
            ),
            0L,
            ClaimsRoutine.DELAY,
            TimeUnit.SECONDS
        );
    }

    @Override
    @SuppressWarnings(
        {
            "PMD.AvoidInstantiatingObjectsInLoops",
            "PMD.ConfusingTernary",
            "PMD.AvoidCatchingGenericException"
        }
    )
    public void run() {
        final boolean full = this.queue.size() + ClaimsRoutine.LIMIT
            >= ClaimsRoutine.QUEUE_SIZE;
        final AmazonSQS sqs = new UncheckedScalar<>(new ExtSqs(this.farm))
            .value();
        final String url =
            new UncheckedText(new ClaimsQueueUrl(this.farm))
                .asString();
        final List<Message> messages = sqs.receiveMessage(
            new ReceiveMessageRequest(url)
                .withMessageAttributeNames(
                    "project", "signature", ClaimsRoutine.UNTIL,
                    "expires", "priority"
                )
                .withVisibilityTimeout(
                    (int) Duration.ofMinutes(2L).getSeconds()
                )
                .withMaxNumberOfMessages(ClaimsRoutine.LIMIT)
        ).getMessages();
        int queued = 0;
        for (final Message message : messages) {
            final Map<String, MessageAttributeValue> attr =
                message.getMessageAttributes();
            if (new MsgExpired(message).value()) {
                sqs.deleteMessage(
                    new DeleteMessageRequest()
                        .withQueueUrl(url)
                        .withReceiptHandle(message.getReceiptHandle())
                );
                Logger.info(
                    this,
                    "Removed expired message: %s",
                    message.getMessageId()
                );
                continue;
            }
            if (full && MsgPriority.from(message).value()
                > MsgPriority.NORMAL.value()) {
                continue;
            }
            attr.put(
                "received",
                new MessageAttributeValue()
                    .withStringValue(Instant.now().toString())
            );
            if (attr.containsKey(ClaimsRoutine.UNTIL)
                && Instant.parse(
                attr.get(ClaimsRoutine.UNTIL).getStringValue()
            ).isAfter(Instant.now())) {
                continue;
            }
            ClaimsRoutine.addClaim(this.queue, message);
            ++queued;
        }
        Logger.info(
            this,
            "received %d messages from SQS, enqueued %d, size %d",
            messages.size(), queued, this.queue.size()
        );
        if (this.queue.size() > Tv.HUNDRED) {
            try {
                this.sanitize(sqs, url);
                // @checkstyle IllegalCatch (1 line)
            } catch (final Exception err) {
                Logger.warn(this, "Sanitize failed: %[exception]s");
            }
        }
    }

    @Override
    public void close() {
        this.service.shutdown();
    }

    /**
     * Local queue of messages ordered by priority.
     * @return Message queue
     */
    public BlockingQueue<Message> messages() {
        return this.queue;
    }

    /**
     * Sanitize messages on high load.
     *
     * @param sqs SQS queue
     * @param url Queue url
     */
    private void sanitize(final AmazonSQS sqs, final String url) {
        final Iterator<Message> iter = this.queue.iterator();
        while (iter.hasNext()) {
            final Message msg = iter.next();
            if (new MsgExpired(msg).value() || ClaimsRoutine.isOldPing(msg)) {
                iter.remove();
                try {
                    sqs.deleteMessage(
                        new DeleteMessageRequest()
                            .withQueueUrl(url)
                            .withReceiptHandle(msg.getReceiptHandle())
                    );
                } catch (final AmazonSQSException err) {
                    Logger.error(
                        this,
                        "Failed to delete expired message: %[exception]s",
                        err
                    );
                    continue;
                }
                Logger.info(
                    this,
                    "Sanitize: removed message: %s",
                    msg.getMessageId()
                );
            }
        }
    }

    /**
     * Check if ping claim is old.
     * @param msg Message
     * @return True if is ping and is old
     */
    private static boolean isOldPing(final Message msg) {
        final ClaimIn claim = new ClaimIn(
            new XMLDocument(msg.getBody())
                .nodes("/claim").get(0)
        );
        final boolean ping = "ping".equalsIgnoreCase(claim.type())
            || "ping hourly".equalsIgnoreCase(claim.type());
        final boolean old = claim.created().toInstant()
            .isBefore(Instant.now().minus(Duration.ofHours(1L)));
        return ping && old;
    }

    /**
     * Remove all duplicates and add new message.
     * @param queue Queue
     * @param msg Message
     */
    private static void addClaim(final Collection<Message> queue,
        final Message msg) {
        queue.removeIf(
            next -> Objects.equals(next.getMessageId(), msg.getMessageId())
        );
        queue.add(msg);
    }

    /**
     * Runnable decorator with shutdown hook check.
     */
    private static final class ShutdownRunnable implements Runnable {

        /**
         * Origin runnable.
         */
        private final Runnable origin;

        /**
         * Shutdown hook.
         */
        private final ShutdownFarm.Hook shutdown;

        /**
         * Ctor.
         *
         * @param origin Origin runnable
         * @param shutdown Shutdown hook
         */
        ShutdownRunnable(final Runnable origin,
            final ShutdownFarm.Hook shutdown) {
            this.origin = origin;
            this.shutdown = shutdown;
        }

        @Override
        public void run() {
            if (this.shutdown.check()) {
                this.origin.run();
            }
        }
    }
}
