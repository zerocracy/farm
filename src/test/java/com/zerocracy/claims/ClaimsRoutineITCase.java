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
import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.proc.SqsProject;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.shutdown.ShutdownFarm;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link ClaimsRoutine}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
public final class ClaimsRoutineITCase {
    @BeforeClass
    public static void checkProps() throws Exception {
        Assume.assumeTrue(
            "sqs credentials are not provided",
            new Props(new PropsFarm()).has("//sqs")
        );
    }

    @Test
    public void receiveMessages() throws Exception {
        final Project project = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(project));
        final AmazonSQS sqs = new ExtSqs(farm).value();
        final String queue = new ClaimsQueueUrl(farm).asString();
        final ClaimsSqs claims = new ClaimsSqs(sqs, queue, project);
        final List<Message> messages = new CopyOnWriteArrayList<>();
        final ClaimsRoutine routine = new ClaimsRoutine(
            farm,
            msgs -> {
                messages.addAll(msgs);
                new And(
                    (Message msg) -> sqs.deleteMessage(
                        queue, msg.getReceiptHandle()
                    ), msgs
                ).value();
            },
            () -> true
        );
        routine.start(new ShutdownFarm.Hook());
        TimeUnit.SECONDS.sleep((long) Tv.FIVE);
        final String type = "test";
        new ClaimOut()
            .type(type)
            .param("nonce1", System.nanoTime())
            .postTo(claims);
        new ClaimOut()
            .type("delayed")
            .until(Duration.ofMinutes(1L))
            .param("nonce2", System.nanoTime())
            .postTo(claims);
        TimeUnit.SECONDS.sleep((long) Tv.FIVE);
        routine.close();
        MatcherAssert.assertThat(
            "expected one message",
            messages,
            Matchers.hasSize(1)
        );
        final Message message = messages.get(0);
        MatcherAssert.assertThat(
            "invalid project",
            new SqsProject(farm, message).pid(),
            Matchers.equalTo(project.pid())
        );
        final ClaimIn claim = new ClaimIn(
            new XMLDocument(message.getBody()).nodes("//claim").get(0)
        );
        MatcherAssert.assertThat(
            claim.type(),
            Matchers.equalTo(type)
        );
    }
}
