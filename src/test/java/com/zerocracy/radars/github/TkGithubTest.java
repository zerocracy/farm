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
package com.zerocracy.radars.github;

import com.zerocracy.farm.props.PropsFarm;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.HttpException;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithBody;

/**
 * Test case for {@link TkGithub}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class TkGithubTest {
    @Test
    public void parsesJson() throws Exception {
        MatcherAssert.assertThat(
            new TkGithub(
                new PropsFarm(),
                (farm, github, event) -> "nothing"
            ).act(
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

    @Test(expected = HttpException.class)
    public void handleNonEncodedPayload() throws Exception {
        new TkGithub(
            new PropsFarm(),
            (farm, github, event) -> "none"
        ).act(
            new RqWithBody(
                new RqFake(),
                "payload={\"x\":\"% r\"}"
            )
        );
    }
}
