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
import com.jcabi.aspects.Tv;
import com.zerocracy.claims.MsgPriority;
import com.zerocracy.farm.fake.FkProject;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ProjectQueue}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ProjectQueueTest {

    @Test
    public void takeByPriorityOrder() throws Exception {
        final List<String> out = new LinkedList<>();
        final ProjectQueue queue = new ProjectQueue(
            new FkProject().pid(), msg -> out.add(msg.getMessageId())
        );
        final String first = "first";
        queue.push(ProjectQueueTest.msg(first, MsgPriority.NORMAL));
        final String second = "second";
        queue.push(ProjectQueueTest.msg(second, MsgPriority.LOW));
        final String third = "third";
        queue.push(ProjectQueueTest.msg(third, MsgPriority.HIGH));
        queue.start();
        while (queue.size() != 0) {
            Thread.sleep((long) Tv.HUNDRED);
        }
        queue.stop();
        MatcherAssert.assertThat(
            out, Matchers.contains(third, first, second)
        );
    }

    private static Message msg(final String mid, final MsgPriority pri) {
        final Message msg = new Message();
        msg.setMessageId(mid);
        msg.getMessageAttributes().put(
            "priority",
            new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(pri.toString())
        );
        return msg;
    }
}
