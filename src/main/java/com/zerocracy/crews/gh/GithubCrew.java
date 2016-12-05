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

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * GitHub notifications listening crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
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
        final String since = new Github.Time(
            DateUtils.addHours(new Date(), -1)
        ).toString();
        final Request req = this.github.entry()
            .uri().path("/notifications").back();
        final List<JsonObject> events =
            StreamSupport.stream(
                new RtPagination<>(
                    req.uri().queryParam("participating", "true")
                        .queryParam("since", since)
                        .queryParam("all", Boolean.toString(true))
                        .back(),
                    RtPagination.COPYING
                ).spliterator(),
                false
            )
//            .filter(json -> "mention".equals(json.getString("reason")))
            .collect(Collectors.toList());
        Logger.info(this, "%d GitHub events found", events.size());
        for (final JsonObject event : events) {
            this.employ(farm, event);
        }
        req.uri()
            .queryParam("last_read_at", since).back()
            .method(Request.PUT)
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
    private void employ(final Farm farm, final JsonObject event)
        throws IOException {
        final Coordinates coords = new Coordinates.Simple(
            event.getJsonObject("repository").getString("full_name")
        );
        final Issue issue = this.github.repos().get(coords).issues().get(
            Integer.parseInt(
                StringUtils.substringAfterLast(
                    event.getJsonObject("subject").getString("url"),
                    "/"
                )
            )
        );
        farm.find(coords.toString()).iterator().next().employ(
            new StkHello(issue)
        );
    }
}
