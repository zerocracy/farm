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
import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimSignature;
import com.zerocracy.claims.Footprint;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.cactoos.func.IoCheckedProc;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FootprintProc}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class FootprintProcTest {
    @Test
    public void footprintClaim() throws Exception {
        final Project project = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(project));
        final AtomicLong cid = new AtomicLong(1L);
        final int threads = 10;
        try (final Footprint footprint = new Footprint(farm, project)) {
            MatcherAssert.assertThat(
                inc -> {
                    final long num = cid.getAndIncrement();
                    new ClaimOut().cid(num)
                        .type("Hello")
                        .param("something", num)
                        .author("0pdd")
                        .postTo(
                            claim -> new IoCheckedProc<>(
                                new FootprintProc(
                                    farm,
                                    msg -> {
                                    }
                                )
                            ).exec(
                                new Message()
                                    .withBody(claim.toString())
                                    .withMessageAttributes(
                                        // @checkstyle LineLength (20 line)
                                        new MapOf<String, MessageAttributeValue>(
                                            new MapEntry<>(
                                                "project",
                                                new MessageAttributeValue()
                                                    .withDataType("String")
                                                    .withStringValue(project.pid())
                                            ),
                                            new MapEntry<>(
                                                "signature",
                                                new MessageAttributeValue()
                                                    .withDataType("String")
                                                    .withStringValue(
                                                        new ClaimSignature(
                                                            claim.nodes("//claim").get(0)
                                                        ).asString()
                                                    )
                                            )
                                        )
                                    )
                            )
                        );
                    return true;
                },
                new RunsInThreads<>(new AtomicInteger(), threads)
            );
            MatcherAssert.assertThat(
                footprint.collection()
                    .find(Filters.eq("project", project.pid())),
                Matchers.iterableWithSize(threads)
            );
        }
    }
}
