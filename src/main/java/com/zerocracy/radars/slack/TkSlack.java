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
package com.zerocracy.radars.slack;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.Bots;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import javax.json.JsonObject;
import org.cactoos.func.AsyncFunc;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.misc.Href;
import org.takes.rq.RqHref;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

/**
 * Slack authentication entry point.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkSlack implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Refresh the SlackRadar asynchronously.
     */
    private final AsyncFunc<Boolean, Boolean> refresh;

    /**
     * Ctor.
     * @param frm Farm
     * @param rdr Radar
     */
    public TkSlack(final Farm frm, final SlackRadar rdr) {
        this.farm = frm;
        this.refresh = new AsyncFunc<Boolean, Boolean>(
            input -> {
                rdr.refresh();
            },
            new VerboseThreads()
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Response act(final Request req) throws IOException {
        final Href href = new RqHref.Base(req).href();
        if (!href.param("code").iterator().hasNext()) {
            throw new RsForward(
                "Slack didn't authorize you, sorry!"
            );
        }
        final Props props = new Props(this.farm);
        final JsonObject json =
            new JdkRequest("https://slack.com/api/oauth.access")
                .uri()
                .queryParam(
                    "client_id",
                    props.get("//slack/client_id")
                )
                .queryParam(
                    "client_secret",
                    props.get("//slack/client_secret")
                )
                .queryParam("code", href.param("code").iterator().next())
                .back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class)
                .json()
                .readObject();
        if (!json.getBoolean("ok")) {
            throw new RsForward(
                String.format(
                    "Slack authentication error: '%s'",
                    json.getString("error")
                )
            );
        }
        final Bots bots = new Bots(this.farm).bootstrap();
        final String team = bots.register(json);
        this.refresh.apply(true);
        return new RsWithStatus(
            new RsWithHeader(
                "Location",
                String.format(
                    "https://%s.slack.com/messages/@0crat/details/",
                    URLEncoder.encode(team, "UTF-8")
                )
            ),
            HttpURLConnection.HTTP_SEE_OTHER
        );
    }

}
