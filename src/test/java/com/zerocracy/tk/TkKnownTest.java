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
package com.zerocracy.tk;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.People;
import java.net.HttpURLConnection;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.FtRemote;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Test case for {@link TkKnown}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkKnownTest {

    /**
     * Zerocrat login.
     */
    private static final String ZEROCRAT = "0crat";

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

    @Test
    public void returnsDetils() throws Exception {
        final Farm farm = new FkFarm();
        final String login = "user15";
        final People people = new People(farm).bootstrap();
        people.invite(login, TkKnownTest.ZEROCRAT);
        people.details(login, "details243564");
        final int rep = 1324;
        people.reputation(login, rep);
        new FtRemote(TkKnownTest.take(farm)).exec(
            base -> new JdkRequest(base)
                .uri().path(String.format("/known/%s", login)).back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(
                    new StringIsJson.Object(
                        Matchers.allOf(
                            new JsonHas("login", new JsonValueIs(login)),
                            new JsonHas("reputation", new JsonValueIs(rep)),
                            new JsonHas("identified", new JsonValueIs(true))
                        )
                    )
                )
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
