/**
 * Copyright (c) 2016 Zerocracy
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

import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.Queue;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Take;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithBody;

/**
 * Test case for {@link TkGhook}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
public final class TkGhookTest {

    /**
     * TkGhook can parse JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesJson() throws Exception {
        final Queue<JsonObject> queue = new LinkedList<>();
        final Take take = new TkGhook(queue);
        MatcherAssert.assertThat(
            take.act(
                new RqWithBody(
                    new RqFake("POST", "/"),
                    "{\"foo\": \"bar\"}"
                )
            ),
            new HmRsStatus(HttpURLConnection.HTTP_OK)
        );
        MatcherAssert.assertThat(
            queue.poll().getString("foo"),
            Matchers.equalTo("bar")
        );
    }
}
