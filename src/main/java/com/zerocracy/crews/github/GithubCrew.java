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
package com.zerocracy.crews.github;

import com.jcabi.github.Github;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.JsonObject;

/**
 * GitHub notifications listening crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @todo #3:30min Current implementation has a big flaw -- it reads
 *  the last message in an issue, not the one posted by the user. Thus,
 *  if there were a few messages after the original one, we will lose
 *  them all together with the original one and will process just
 *  the latest one in the thread.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class GithubCrew implements Crew {

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Reaction.
     */
    private final Reaction reaction;

    /**
     * Ctor.
     * @param ghb Github client
     */
    public GithubCrew(final Github ghb) {
        this.github = ghb;
        this.reaction = new ReLogged(
            new Reaction.Chain(
                new ReOnReason("invitation", new ReOnInvitation(ghb)),
                new ReOnReason(
                    "mention",
                    new ReOnComment(
                        ghb,
                        new ReNotMine(
                            new Response.Chain(
                                new ReRegex("hello", new ReHello()),
                                new ReRegex("in", new ReIn()),
                                new ReRegex("out", new ReOut()),
                                new ReRegex(".*", new ReSorry())
                            )
                        )
                    )
                )
            )
        );
    }

    @Override
    public void deploy(final Farm farm) throws IOException {
        final Request req = this.github.entry()
            .uri().path("/notifications").back();
        final Iterable<JsonObject> events =
            new RtPagination<>(req, RtPagination.COPYING);
        for (final JsonObject event : events) {
            this.reaction.react(farm, event);
        }
        req.method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
    }

}
