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
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtSqs;
import java.io.IOException;
import org.cactoos.Text;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.SolidFunc;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Queue url.
 * <p>
 * Find queue by name or create if not exist and return its url.
 *
 * @since 1.0
 */
public final class ClaimsQueueUrl implements Text {

    /**
     * Queue name.
     */
    private static final String NAME = "0crat-farm.fifo";

    /**
     * Queue factory.
     */
    private static final IoCheckedFunc<Farm, String> QUEUE =
        new IoCheckedFunc<>(
            new SolidFunc<>(
                frm -> {
                    final AmazonSQS sqs = new ExtSqs(frm).value();
                    String url;
                    try {
                        url = sqs.getQueueUrl(ClaimsQueueUrl.NAME)
                            .getQueueUrl();
                    } catch (final QueueDoesNotExistException ignored) {
                        url = sqs.createQueue(
                            new CreateQueueRequest(ClaimsQueueUrl.NAME)
                                .withAttributes(
                                    new MapOf<String, String>(
                                        new MapEntry<>(
                                            "FifoQueue",
                                            Boolean.toString(true)
                                        ),
                                        new MapEntry<>(
                                            "ReceiveMessageWaitTimeSeconds",
                                            "10"
                                        )
                                    )
                                )
                        ).getQueueUrl();
                    }
                    return url;
                }
            )
        );

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public ClaimsQueueUrl(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public String asString() throws IOException {
        return ClaimsQueueUrl.QUEUE.apply(this.farm);
    }
}
