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
import com.jcabi.aspects.Tv;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimXml;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SkipStaleProc}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class SkipStaleProcTest {

    @Test
    public void skipStale() throws Exception {
        final List<Message> messages = new LinkedList<>();
        new SkipStaleProc(
            messages::add
        ).exec(
            new Message()
                .withBody(
                    new ClaimXml(
                        new ClaimOut(
                            new Date(
                                Instant.now()
                                    .minus(Duration.ofMinutes((long) Tv.SIX))
                                    .toEpochMilli()
                            )
                        ).type("Ping")
                    ).asXml().toString()
                )
        );
        MatcherAssert.assertThat(
            messages,
            Matchers.empty()
        );
    }

    @Test
    public void processActual() throws Exception {
        final List<Message> messages = new LinkedList<>();
        new SkipStaleProc(
            messages::add
        ).exec(
            new Message()
                .withBody(
                    new ClaimXml(
                        new ClaimOut(
                            new Date(
                                Instant.now()
                                    .minus(Duration.ofMinutes((long) Tv.FOUR))
                                    .toEpochMilli()
                            )
                        ).type("Ping")
                    ).asXml().toString()
                )
        );
        MatcherAssert.assertThat(
            messages,
            Matchers.hasSize(1)
        );
    }
}
