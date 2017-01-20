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
package com.zerocracy.radars.ghook;

import com.jcabi.github.mock.MkGithub;
import com.zerocracy.jstk.fake.FkFarm;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.Take;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithBody;

/**
 * Test case for {@link GhookRadar}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
public final class GhookRadarTest {

    /**
     * TkGhook can parse JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesJson() throws Exception {
        final Take take = new GhookRadar(new FkFarm(), new MkGithub());
        MatcherAssert.assertThat(
            take.act(
                new RqWithBody(
                    new RqFake("POST", "/"),
                    String.format(
                        "payload=%s",
                        URLEncoder.encode(
                            "{\"foo\": \"bar\"}",
                            StandardCharsets.UTF_8.displayName()
                        )
                    )
                )
            ),
            new HmRsStatus(HttpURLConnection.HTTP_OK)
        );
    }
}
