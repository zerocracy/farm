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
package com.zerocracy.tk;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.s3.fake.FkBucket;
import java.nio.charset.StandardCharsets;
import org.cactoos.io.InputStreamOf;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.rq.RqFake;

/**
 * Test cases for {@link TkDump}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public class TkDumpTest {

    @Test
    public final void usesBucket() throws Exception {
        final FkBucket bucket = new FkBucket();
        final String content = "test-content";
        bucket
            .ocket("heapdump.hprof")
            .write(
                new InputStreamOf(content, StandardCharsets.UTF_8),
                new ObjectMetadata()
            );
        MatcherAssert.assertThat(
            new TextOf(
                new TkDump(() -> bucket)
                    .act(new RqFake()).body()
            ).asString(),
            Matchers.is(content)
        );
    }
}
