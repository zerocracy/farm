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
package com.zerocracy.pm;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.Claims;
import com.zerocracy.claims.ClaimsSqs;
import com.zerocracy.entry.PropsAwsCredentials;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.matchers.MatcherOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link ClaimsSqs}.
 * <p>
 * To start this test you have to create AWS user with full access to
 * SQS queues policy and insert credentials to test {@code _props.xml}:
 * <pre><code>
 * &lt;props&gt;
 *     &lt;sqs&gt;
 *         &lt;key&gt;your-key&lt;/key&gt;
 *         &lt;secret&gt;your-secret&lt;/secret&gt;
 *     &lt;/sqs&gt;
 * &lt;/props&gt;
 * </code></pre>
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class ClaimsSqsITCase {

    /**
     * SQS client.
     */
    private AmazonSQS client;

    /**
     * Queue url.
     */
    private String queue;

    @BeforeClass
    public static void checkProps() throws Exception {
        Assume.assumeTrue(
            "sqs credentials are not provided",
            new Props(new PropsFarm()).has("//sqs")
        );
    }

    @Before
    public void setUp() {
        this.client = AmazonSQSClient.builder()
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new PropsAwsCredentials(new PropsFarm(), "sqs")
                )
            ).withRegion(Regions.DEFAULT_REGION)
            .build();
        this.queue = this.client
            .createQueue(
                new CreateQueueRequest(
                    String.format(
                        "%s.fifo",
                        UUID.randomUUID()
                    )
                ).withAttributes(
                    new MapOf<String, String>(
                        new MapEntry<>(
                            "FifoQueue",
                            Boolean.toString(true)
                        )
                    )
                )
            ).getQueueUrl();
        Logger.info(this, "Created queue: %s", this.queue);
    }

    @After
    public void tearDown() {
        this.client.deleteQueue(this.queue);
        Logger.info(this, "Queue destroyed: %s", this.queue);
    }

    @Test
    public void sendAndReceiveClaim() throws Exception {
        final Claims claims = new ClaimsSqs(
            this.client, this.queue, new FkProject()
        );
        final String type = "test1";
        new ClaimOut()
            .type(type)
            .postTo(claims);
        final List<Message> messages = this.client.receiveMessage(
            new ReceiveMessageRequest(this.queue)
                .withMaxNumberOfMessages(Tv.TEN)
        ).getMessages();
        MatcherAssert.assertThat(
            messages,
            Matchers.hasSize(1)
        );
    }

    @Test
    public void ignoresDuplicateClaims() throws Exception {
        final Claims claims = new ClaimsSqs(
            this.client, this.queue, new FkProject()
        );
        final int limit = 10;
        final ClaimOut claim = new ClaimOut().type("duplicates");
        for (int num = 0; num < limit; ++num) {
            claim.postTo(claims);
        }
        final List<Message> messages = this.client.receiveMessage(
            new ReceiveMessageRequest(this.queue)
                .withMaxNumberOfMessages(limit)
        ).getMessages();
        MatcherAssert.assertThat(
            messages,
            Matchers.hasSize(1)
        );
    }

    @Test
    public void sendMultiple() throws Exception {
        final Claims claims = new ClaimsSqs(
            this.client, this.queue, new FkProject()
        );
        final int limit = 10;
        final ClaimOut claim = new ClaimOut().type("test2");
        for (int num = 0; num < limit; ++num) {
            claim.param("num", num).postTo(claims);
        }
        final List<Message> messages = this.client.receiveMessage(
            new ReceiveMessageRequest(this.queue)
                .withMaxNumberOfMessages(limit)
        ).getMessages();
        MatcherAssert.assertThat(
            messages,
            Matchers.hasSize(limit)
        );
    }

    @Test
    public void submitWithDelay() throws Exception {
        final Claims claims = new ClaimsSqs(
            this.client, this.queue, new FkProject()
        );
        final Duration delay = Duration.ofSeconds((long) Tv.THIRTY);
        new ClaimOut()
            .type("delayed")
            .until(delay)
            .postTo(claims);
        final String until = "until";
        MatcherAssert.assertThat(
            "received claim with delay",
            this.client.receiveMessage(
                new ReceiveMessageRequest(this.queue)
                    .withMaxNumberOfMessages(Tv.TEN)
                    .withMessageAttributeNames(until)
            ).getMessages(),
            Matchers.contains(
                new MatcherOf<>(
                    (Message msg) -> msg.getMessageAttributes()
                        .containsKey(until)
                )
            )
        );
    }

    @Test
    public void submitWithExpiry() throws Exception {
        final Claims claims = new ClaimsSqs(
            this.client, this.queue, new FkProject()
        );
        final Instant expire = Instant.now().plus(Duration.ofDays(1));
        new ClaimOut()
            .type("Ping")
            .postTo(claims, expire);
        final String attr = "expire";
        MatcherAssert.assertThat(
            "received claim with expiry",
            this.client.receiveMessage(
                new ReceiveMessageRequest(this.queue)
                    .withMaxNumberOfMessages(Tv.TEN)
                    .withMessageAttributeNames(attr)
            ).getMessages(),
            new IsCollectionContaining<>(
                new MatcherOf<>(
                    (Message msg) -> msg.getMessageAttributes()
                        .containsKey(attr)
                )
            )
        );
    }
}
