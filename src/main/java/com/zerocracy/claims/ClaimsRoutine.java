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
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.radars.github.GithubRoutine;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.Proc;
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
     * Messages limit.
     */
    private static final int LIMIT = 4;

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
    private final Proc<Message> proc;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param proc Proc
     */
    public ClaimsRoutine(final Farm farm, final Proc<Message> proc) {
        this.proc = proc;
        this.service = Executors.newSingleThreadScheduledExecutor(
            new VerboseThreads(GithubRoutine.class)
        );
        this.farm = farm;
    }

    /**
     * Start routine.
     */
    public void start() {
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(this),
            0L,
            ClaimsRoutine.DELAY,
            TimeUnit.SECONDS
        );
    }

    @Override
    public void run() {
        final AmazonSQS sqs = new UncheckedScalar<>(new ExtSqs(this.farm))
            .value();
        final String queue = new UncheckedText(new ClaimsQueueUrl(this.farm))
            .asString();
        final List<Message> messages = sqs.receiveMessage(
            new ReceiveMessageRequest(queue)
                .withMessageAttributeNames("project", "signature")
                .withMaxNumberOfMessages(ClaimsRoutine.LIMIT)
        ).getMessages();
        Logger.info(
            this, "received %d messages from SQS", messages.size()
        );
        new UncheckedScalar<>(new And(this.proc, messages)).value();
    }

    @Override
    public void close() {
        this.service.shutdown();
    }
}
