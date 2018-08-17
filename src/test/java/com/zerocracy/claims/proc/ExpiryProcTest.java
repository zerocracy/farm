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
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ExpiryProc}.
 * @since 1.0
 * @checkstyle JavadocMethod (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ExpiryProcTest {

    @Test
    public void acceptsNonExpiredMessage() throws Exception {
        final AtomicBoolean result = new AtomicBoolean(false);
        new ExpiryProc((msg) -> result.set(true)).exec(
            new Message().withMessageAttributes(
                Collections.singletonMap(
                    "expires",
                    new MessageAttributeValue().withStringValue(
                        Instant.now().plus(Duration.ofDays(1)).toString()
                    )
                )
            )
        );
        MatcherAssert.assertThat(
            result.get(), Matchers.is(true)
        );
    }

    @Test
    public void acceptsMessageWithNoExpiry() throws Exception {
        final AtomicBoolean result = new AtomicBoolean(false);
        new ExpiryProc((msg) -> result.set(true)).exec(new Message());
        MatcherAssert.assertThat(
            result.get(), Matchers.is(true)
        );
    }

    @Test
    public void rejectsExpiredMessage() throws Exception {
        final AtomicBoolean result = new AtomicBoolean(false);
        new ExpiryProc((msg) -> result.set(true)).exec(
            new Message().withMessageAttributes(
                Collections.singletonMap(
                    "expires",
                    new MessageAttributeValue().withStringValue(
                        Instant.now().minus(Duration.ofMinutes(1)).toString()
                    )
                )
            )
        );
        MatcherAssert.assertThat(
            result.get(), Matchers.is(false)
        );
    }
}
