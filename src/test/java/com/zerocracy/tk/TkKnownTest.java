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

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.People;
import java.net.HttpURLConnection;
import org.junit.Test;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.FtRemote;

/**
 * Test case for {@link TkKnown}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class TkKnownTest {

    @Test
    public void returnsOkIfFound() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "user1";
        new People(farm).bootstrap().invite(login, "0crat");
        new FtRemote(TkKnownTest.take(farm)).exec(
            base -> new JdkRequest(base)
                .uri().path(String.format("/known/%s", login)).back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
        );
    }

    @Test
    public void returnsNotFoundIfNotFound() throws Exception {
        new FtRemote(TkKnownTest.take(new FkFarm())).exec(
            base -> new JdkRequest(base)
                .uri().path("/known/unknown").back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
        );
    }

    private static Take take(final Farm farm) {
        return new TkFork(
            new FkRegex(
                "/known/(?<login>[a-zA-Z0-9-]+)",
                new TkKnown(farm)
            )
        );
    }
}
