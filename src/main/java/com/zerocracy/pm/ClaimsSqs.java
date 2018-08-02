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
package com.zerocracy.pm;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Project;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.cactoos.Proc;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapOf;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;

/**
 * Claims queue on Amazon SQS.
 *
 * @since 1.0
 * @todo #1480:30min Prevent claims duplication, let's add claim's hash
 *  based on it's content to claims attributes, save it in footprint
 *  and check before processing new claim.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ClaimsSqs implements Claims {

    /**
     * SQS client.
     */
    private final AmazonSQS sqs;

    /**
     * Queue url.
     */
    private final String queue;

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     *
     * @param sqs SQS client
     * @param queue Queue url
     * @param project Project
     */
    public ClaimsSqs(final AmazonSQS sqs, final String queue,
        final Project project) {
        this.sqs = sqs;
        this.queue = queue;
        this.project = project;
    }

    @Override
    public void take(final Proc<XML> proc, final int limit)
        throws IOException {
        new IoCheckedScalar<>(
            new And(
                entry -> {
                    proc.exec(entry.getValue().nodes("/claim").get(0));
                    this.sqs.deleteMessage(
                        this.queue, entry.getKey().getReceiptHandle()
                    );
                },
                new MapOf<>(
                    msg -> msg,
                    msg -> new XMLDocument(msg.getBody()),
                    this.sqs.receiveMessage(
                        new ReceiveMessageRequest(this.queue)
                            .withMaxNumberOfMessages(limit)
                    ).getMessages()
                ).entrySet()
            )
        ).value();
    }

    @Override
    public void submit(final XML claim) throws IOException {
        final SendMessageRequest msg = new SendMessageRequest(
            this.queue,
            claim.toString()
        ).withMessageGroupId(this.project.pid());
        final long until = new IoCheckedScalar<>(
            new ItemAt<>(
                0L,
                new Mapped<>(
                    Long::parseLong,
                    claim.xpath("/claim/until/text()")
                )
            )
        ).value();
        final long delay = Duration.between(
            Instant.now(),
            Instant.ofEpochSecond(until)
        ).getSeconds();
        if (delay > 0L) {
            msg.setDelaySeconds((int) delay);
        }
        this.sqs.sendMessage(msg);
        new ClaimsItem(this.project).bootstrap();
    }
}
