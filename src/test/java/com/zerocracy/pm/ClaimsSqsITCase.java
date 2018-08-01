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
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.entry.PropsAwsCredentials;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link ClaimsSqs}.
 * <p>
 * See {@link com.zerocracy.entry.ClaimsOfITCase} for usage.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
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
                        // @checkstyle LineLength (2 lines)
                        new MapEntry<>("FifoQueue", Boolean.toString(true)),
                        new MapEntry<>("ContentBasedDeduplication", Boolean.toString(true))
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
        final String type = "test";
        new ClaimOut()
            .type(type)
            .postTo(claims);
        final List<ClaimIn> received = new CopyOnWriteArrayList<>();
        claims.take(xml -> received.add(new ClaimIn(xml)), Tv.TEN);
        MatcherAssert.assertThat(
            received,
            Matchers.hasSize(1)
        );
        final ClaimIn claim = received.get(0);
        MatcherAssert.assertThat(
            claim.type(),
            Matchers.equalTo(type)
        );
    }
}
