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

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimsQueueUrl;
import com.zerocracy.entry.ExtSqs;
import org.cactoos.Proc;

/**
 * Delete message in SQS queue.
 *
 * @since 1.0
 */
public final class DeleteProc implements Proc<Message> {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Origin.
     */
    private final Proc<Message> origin;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param origin Origin
     */
    public DeleteProc(final Farm farm, final Proc<Message> origin) {
        this.farm = farm;
        this.origin = origin;
    }

    @Override
    public void exec(final Message input) throws Exception {
        new ExtSqs(this.farm).value().deleteMessage(
            new DeleteMessageRequest()
                .withQueueUrl(new ClaimsQueueUrl(this.farm).asString())
                .withReceiptHandle(input.getReceiptHandle())
        );
        Logger.info(
            this, "Message %s was deleted", input.getMessageId()
        );
        this.origin.exec(input);
    }
}
