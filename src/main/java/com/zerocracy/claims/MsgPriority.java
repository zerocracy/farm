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

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import java.util.Locale;
import java.util.Map;

/**
 * Message claim priority.
 *
 * @since 1.0
 */
public enum MsgPriority {
    /**
     * Low priority - routine claims, pings, maintenance jobs.
     */
    LOW(3),
    /**
     * Default message priority, repo hooks.
     */
    NORMAL(2),
    /**
     * High priority - user actions, chat messages.
     */
    HIGH(1);

    /**
     * Message priority.
     */
    private static final String MSG_ATTR = "priority";

    /**
     * Priority.
     */
    private final int val;

    /**
     * Ctor.
     * @param val Priority
     */
    MsgPriority(final int val) {
        this.val = val;
    }

    /**
     * Priority value, less means more important.
     * @return Number
     */
    public int value() {
        return this.val;
    }

    /**
     * Read priority from SQS message.
     * @param msg Message
     * @return Priority of message
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static MsgPriority from(final Message msg) {
        final Map<String, MessageAttributeValue> attr =
            msg.getMessageAttributes();
        final MsgPriority res;
        if (attr.containsKey(MsgPriority.MSG_ATTR)) {
            res = MsgPriority.valueOf(
                attr.get(MsgPriority.MSG_ATTR).getStringValue()
                    .toUpperCase(Locale.US)
            );
        } else {
            res = MsgPriority.NORMAL;
        }
        return res;
    }
}
