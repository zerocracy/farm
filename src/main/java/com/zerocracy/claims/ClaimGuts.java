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
import com.jcabi.xml.XMLDocument;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Claim guts XML.
 *
 * @since 1.0
 */
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals",
        "PMD.AvoidInstantiatingObjectsInLoops"
    }
)
public final class ClaimGuts implements Iterable<Directive> {

    /**
     * Current messages.
     */
    private final List<Iterable<Message>> queues;

    /**
     * Ctor.
     */
    public ClaimGuts() {
        this.queues = new LinkedList<>();
    }

    @Override
    public Iterator<Directive> iterator() {
        final Directives dirs = new Directives();
        dirs.add("queues");
        for (final Iterable<Message> queue : this.queues) {
            for (final Message message : queue) {
                dirs.add("queue");
                final ClaimIn claim = new ClaimIn(
                    new XMLDocument(message.getBody())
                        .nodes("/claim").get(0)
                );
                final Map<String, MessageAttributeValue> attr =
                    message.getMessageAttributes();
                final Directives msg = dirs.add("message")
                    .attr("id", message.getMessageId())
                    .add("claim").attr("id", claim.cid())
                    .set(claim.type())
                    .up()
                    .add("project")
                    .set(attr.get("project").getStringValue())
                    .up()
                    .add("received")
                    .set(attr.get("received").getStringValue())
                    .up()
                    .add("priority")
                    .set(MsgPriority.from(message))
                    .up();
                if (attr.containsKey("expires")) {
                    msg.add("expires")
                        .set(attr.get("expires").getStringValue())
                        .up();
                }
                msg.up();
                dirs.up();
            }
        }
        dirs.up();
        return dirs.iterator();
    }

    /**
     * Start processing input.
     *
     * @param queue Message queue
     */
    public void add(final Iterable<Message> queue) {
        this.queues.add(queue);
    }
}
