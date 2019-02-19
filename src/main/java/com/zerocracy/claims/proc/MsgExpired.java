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

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import java.time.Instant;
import java.util.Map;
import org.cactoos.Scalar;

/**
 * Message expired.
 *
 * @since 1.0
 */
public final class MsgExpired implements Scalar<Boolean> {

    /**
     * Expires attr.
     */
    private static final String KEY_EXPIRES = "expires";

    /**
     * Message.
     */
    private final Message msg;

    /**
     * Ctor.
     * @param msg Message
     */
    public MsgExpired(final Message msg) {
        this.msg = msg;
    }

    @Override
    public Boolean value() {
        final Map<String, MessageAttributeValue> attr =
            this.msg.getMessageAttributes();
        final boolean expired;
        if (attr.containsKey(MsgExpired.KEY_EXPIRES)) {
            expired = Instant.parse(
                attr.get(MsgExpired.KEY_EXPIRES).getStringValue()
            ).isBefore(Instant.now());
        } else {
            expired = false;
        }
        return expired;
    }
}
