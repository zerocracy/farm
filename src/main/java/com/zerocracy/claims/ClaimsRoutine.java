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
package com.zerocracy.claims;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.shutdown.ShutdownFarm;
import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.scalar.And;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.UncheckedText;

/**
 * Claims routine.
 * <p>
 * This class uses long-polling to fetch claims from SQS queue.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ClaimsRoutine implements Runnable, Closeable {
    /**
     * Until attribute.
     */
    private static final String UNTIL = "until";

    /**
     * Messages limit.
     */
    private static final int LIMIT = 8;

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
     * Process queue messages.
     */
    private final Proc<List<Message>> proc;

    /**
     * If we can proceed with execution.
     */
    private final Scalar<Boolean> proceed;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param proc Proc
     * @param proceed A flag that signals if the processing can proceed
     */
    public ClaimsRoutine(final Farm farm, final Proc<List<Message>> proc,
        final Scalar<Boolean> proceed) {
        this.proc = proc;
        this.proceed = proceed;
        this.service = Executors.newSingleThreadScheduledExecutor(
            new VerboseThreads(ClaimsRoutine.class)
        );
        this.farm = farm;
    }

    /**
     * Start routine.
     *
     * @param shutdown Shutdown hook
     */
    public void start(final ShutdownFarm.Hook shutdown) {
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(new ShutdownRunnable(this, shutdown)),
            0L,
            ClaimsRoutine.DELAY,
            TimeUnit.SECONDS
        );
    }

    @Override
    @SuppressWarnings(
        {
            "PMD.AvoidInstantiatingObjectsInLoops",
            "PMD.AvoidDuplicateLiterals",
            "PMD.ConfusingTernary"
        }
    )
    public void run() {
        if (new UncheckedScalar<>(this.proceed).value()) {
            final AmazonSQS sqs = new UncheckedScalar<>(new ExtSqs(this.farm))
                .value();
            final String queue =
                new UncheckedText(new ClaimsQueueUrl(this.farm))
                    .asString();
            final List<Message> messages = sqs.receiveMessage(
                new ReceiveMessageRequest(queue)
                    .withMessageAttributeNames(
                        "project", "signature", ClaimsRoutine.UNTIL, "expires"
                    )
                    .withVisibilityTimeout(
                        (int) Duration.ofMinutes(2).getSeconds()
                    )
                    .withMaxNumberOfMessages(ClaimsRoutine.LIMIT)
            ).getMessages();
            final Set<String> projects = new HashSet<>();
            final List<Message> merged = new LinkedList<>();
            for (final Message message : messages) {
                final Map<String, MessageAttributeValue> attr =
                    message.getMessageAttributes();
                if (attr.containsKey(ClaimsRoutine.UNTIL)
                    && Instant.parse(
                        attr.get(ClaimsRoutine.UNTIL).getStringValue()
                    ).isAfter(Instant.now())) {
                    continue;
                }
                final XML xml = new XMLDocument(message.getBody())
                    .nodes("/claim").get(0);
                final String pid = attr
                    .get("project")
                    .getStringValue();
                final ClaimIn claim = new ClaimIn(xml);
                final boolean ping = "ping".equalsIgnoreCase(claim.type());
                if (ping && !projects.contains(pid)) {
                    projects.add(pid);
                    merged.add(message);
                } else if (!ping) {
                    merged.add(message);
                } else {
                    sqs.deleteMessage(queue, message.getReceiptHandle());
                }
            }
            Logger.info(
                this, "received %d (%d actual) messages from SQS",
                messages.size(), merged.size()
            );
            new UncheckedScalar<>(new And(this.proc, merged)).value();
        }
    }

    @Override
    public void close() {
        this.service.shutdown();
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
