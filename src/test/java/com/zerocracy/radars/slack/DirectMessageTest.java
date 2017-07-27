/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.radars.slack;

import org.cactoos.TextHasString;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link DirectMessage}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.13
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class DirectMessageTest {

    @Test
    public void withMentionTest() throws Exception {
        MatcherAssert.assertThat(
            "Didn't skip mention",
            new DirectMessage("@0crat version"),
            new TextHasString("version")
        );
    }

    @Test
    public void withoutMentionTest() throws Exception {
        final String msg = "hello";
        MatcherAssert.assertThat(
            "Changed origin message",
            new DirectMessage(msg),
            new TextHasString(msg)
        );
    }
}
