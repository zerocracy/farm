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
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link ClaimsSqs}.
 * <p>
 * See {@link com.zerocracy.entry.ClaimsOfITCase} for usage.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@Ignore
public final class ClaimsSqsITCase {

    /**
     * SQS AWS key.
     */
    private static final String SQS_KEY = "";

    /**
     * SQS AWS secret.
     */
    private static final String SQS_SECRET = "";

    /**
     * SQS client.
     */
    private AmazonSQS client;
    /**
     * Queue url.
     */
    private String queue;

    @Before
    public void setUp() {
        this.client = AmazonSQSClient.builder()
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        ClaimsSqsITCase.SQS_KEY,
                        ClaimsSqsITCase.SQS_SECRET
                    )
                )
            ).withRegion(Regions.DEFAULT_REGION)
            .build();
        this.queue = this.client
            .createQueue(UUID.randomUUID().toString()).getQueueUrl();
        Logger.info(this, "Created queue: %s", this.queue);
    }

    @After
    public void tearDown() {
        this.client.deleteQueue(this.queue);
        Logger.info(this, "Queue destroyed: %s", this.queue);
    }

    @Test
    public void sendAndReceiveClaim() throws Exception {
        final Claims claims = new ClaimsSqs(this.client, this.queue);
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
