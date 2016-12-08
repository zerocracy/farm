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
package com.zerocracy.crews.gh;

import com.google.common.collect.ImmutableMap;
import com.jcabi.github.Github;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * GitHub notifications listening crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class GithubCrew implements Crew {

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Ctor.
     * @param ghb Github client
     */
    public GithubCrew(final Github ghb) {
        this.github = ghb;
    }

    @Override
    public void deploy(final Farm farm) throws IOException {
        final Request req = this.github.entry()
            .uri().path("/notifications").back();
        final List<Event> events =
            StreamSupport.stream(
                new RtPagination<>(req, RtPagination.COPYING).spliterator(),
                false
            )
            .map(json -> new Event(this.github, json))
            .collect(Collectors.toList());
        events.forEach(event -> Logger.info(this, "%s", event));
        for (final Event event : events) {
            GithubCrew.employ(farm, event);
        }
        req.method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
    }

    /**
     * Event to parse and employ a stakeholder.
     * @param farm Farm
     * @param event JSON event from GitHub
     * @throws IOException If fails
     */
    private static void employ(final Farm farm, final Event event)
        throws IOException {
        final Project project = farm
            .find(event.coordinates().toString())
            .iterator()
            .next();
        project.employ(
            new StkChain(
                new StkByReason(
                    event,
                    "mention",
                    new StkNotMine(
                        event,
                        new StkByCommand(
                            event,
                            ImmutableMap.<String, Stakeholder>builder()
                                .put("hello", new StkHello(event))
                                .put(".*", new StkSorry(event))
                                .build()
                        )
                    )
                )
            )
        );
    }
}
