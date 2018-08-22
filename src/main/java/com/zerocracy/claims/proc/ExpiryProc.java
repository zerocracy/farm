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

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import java.time.Instant;
import java.util.Map;
import org.cactoos.Proc;

/**
 * Proc that checks for claims expiry.
 *
 * @since 1.0
 */
public final class ExpiryProc implements Proc<Message> {
    /**
     * Expires attribute.
     */
    private static final String KEY_EXPIRES = "expires";

    /**
     * Decorated proc.
     */
    private final Proc<Message> proc;

    /**
     * Ctor.
     * @param origin Original proc
     */
    public ExpiryProc(final Proc<Message> origin) {
        this.proc = origin;
    }

    @Override
    public void exec(final Message input) throws Exception {
        final Map<String, MessageAttributeValue> attrs =
            input.getMessageAttributes();
        if (attrs.containsKey(ExpiryProc.KEY_EXPIRES)) {
            final Instant expiry = Instant.parse(
                attrs.get(ExpiryProc.KEY_EXPIRES).getStringValue()
            );
            if (Instant.now().isAfter(expiry)) {
                return;
            }
        }
        this.proc.exec(input);
    }
}
