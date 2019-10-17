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
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;

/**
 * Claims queue on Amazon SQS.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ClaimsSqs implements Claims {

    /**
     * Farm.
     */
    private final Farm farm;

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
     * @param farm Farm
     * @param sqs SQS client
     * @param queue Queue url
     * @param project Project
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public ClaimsSqs(final Farm farm,
        final AmazonSQS sqs, final String queue,
        final Project project) {
        this.sqs = sqs;
        this.queue = queue;
        this.project = project;
        this.farm = farm;
    }

    @Override
    public void submit(final XML claim, final Instant expires)
        throws IOException {
        final String type = claim.xpath("/claim/type/text()").get(0);
        if (this.skip(claim)) {
            Logger.info(
                this,
                "claims queue is too big, skipping claim %s",
                type
            );
            return;
        }
        final SendMessageRequest msg = new SendMessageRequest(
            this.queue, claim.toString()
        ).withMessageGroupId(this.group(claim));
        final Map<String, MessageAttributeValue> attrs = new HashMap<>(1);
        final String signature = new ClaimSignature(
            claim.nodes("//claim").get(0)
        ).asString();
        if (!expires.equals(Instant.MAX)) {
            attrs.put(
                "expires",
                new MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(expires.toString())
            );
        }
        attrs.put(
            "signature",
            new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(signature)
        );
        attrs.put(
            "project",
            new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(this.project.pid())
        );
        new IoCheckedScalar<>(
            new And(
                (String delay) -> attrs.put(
                    "until",
                    new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(delay)
                ),
                claim.xpath("/claim/until/text()")
            )
        ).value();
        new IoCheckedScalar<>(
            new And(
                (String priority) -> attrs.put(
                    "priority",
                    new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(priority)
                ),
                claim.xpath("/claim/params/param[@name='priority']/text()")
            )
        ).value();
        msg.setMessageDeduplicationId(
            String.format(
                "%s:%s",
                this.project.pid(),
                signature
            )
        );
        msg.setMessageAttributes(attrs);
        Logger.debug(this, "sending message: %s", msg);
        final SendMessageResult res = this.sqs.sendMessage(msg);
        Logger.info(
            this,
            "Claim '%s' (%s) was send: mid=%s",
            claim.xpath("/claim/@id").get(0),
            type, res
        );
    }

    @Override
    public void submit(final XML claim) throws IOException {
        this.submit(claim, Instant.MAX);
    }

    /**
     * Message group id.
     * @param claim Claim XML
     * @return Group ID string
     * @throws IOException If fals
     */
    private String group(final XML claim) throws IOException {
        final String group;
        final String type = claim.xpath("/claim/type/text()").get(0);
        if (type.toLowerCase(Locale.US).startsWith("ping")) {
            group = String.format("pings:%s", this.project.pid());
        } else {
            final String cid = claim.xpath("/claim/@id").get(0);
            group = String.format("claim:%s", cid);
        }
        return group;
    }

    /**
     * Can we skip this claim.
     * @param claim Claim
     * @return True if skip
     * @throws IOException If fails
     */
    private boolean skip(final XML claim) throws IOException {
        final long size = new IoCheckedScalar<>(
            new IoCheckedScalar<>(new SqsQueueSize(this.farm))
        ).value();
        final String type = claim.xpath("/claim/type/text()").get(0);
        // @checkstyle MagicNumberCheck (1 line)
        return size > 256L
            // @checkstyle LineLengthCheck (1 line)
            && ("ping".equalsIgnoreCase(type) || "ping hourly".equalsIgnoreCase(type));
    }
}
