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
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.farm.props.Props;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;

/**
 * SQS queue approximate size.
 * <p>
 * Auto updated every minute.
 * </p>
 *
 * @since 1.0
 */
public final class SqsQueueSize implements Scalar<Long> {

    /**
     * Auto updated memory with queue size factory.
     */
    private static final Func<Farm, AtomicLong> FACTORY = new SolidFunc<>(
        frm -> {
            final AtomicLong mem = new AtomicLong();
            if (!new Props(frm).has("//testing")) {
                final AmazonSQS sqs = new ExtSqs(frm).value();
                final String url = new ClaimsQueueUrl(frm).asString();
                Executors.newSingleThreadScheduledExecutor()
                    .scheduleWithFixedDelay(
                        () -> {
                            // @checkstyle LineLengthCheck (5 lines)
                            final String mnum = sqs.getQueueAttributes(
                                new GetQueueAttributesRequest(url)
                                    .withAttributeNames(QueueAttributeName.ApproximateNumberOfMessages)
                            ).getAttributes().get(QueueAttributeName.ApproximateNumberOfMessages.toString());
                            mem.set(Long.parseLong(mnum));
                        },
                        0L, 1L, TimeUnit.MINUTES
                    );
            }
            return mem;
        }
    );

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public SqsQueueSize(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Long value() throws Exception {
        return SqsQueueSize.FACTORY.apply(this.farm).get();
    }
}
