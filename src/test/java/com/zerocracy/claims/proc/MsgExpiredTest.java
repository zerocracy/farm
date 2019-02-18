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
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

/**
 * Test case for {@link MsgExpired}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class MsgExpiredTest {

    @Test
    public void messageExpired() {
        MatcherAssert.assertThat(
            new MsgExpired(
                MsgExpiredTest.message(
                    Instant.now().minus(Duration.ofMinutes(1L))
                )
            ).value(),
            new IsEqual<>(true)
        );
    }

    @Test
    public void messageNotExpired() {
        MatcherAssert.assertThat(
            new MsgExpired(
                MsgExpiredTest.message(
                    Instant.now().plus(Duration.ofMinutes(1L))
                )
            ).value(),
            new IsEqual<>(false)
        );
    }

    private static Message message(final Instant expires) {
        return new Message()
            .withMessageAttributes(
                Collections.singletonMap(
                    "expires",
                    new MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(expires.toString())
                )
            );
    }
}
