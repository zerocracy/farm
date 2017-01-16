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

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pmo.Bots;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Properties;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHref;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;

/**
 * Slack callback page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkSlack implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Properties.
     */
    private final Properties properties;

    /**
     * Ctor.
     * @param frm Farm
     * @param props Props
     */
    TkSlack(final Farm frm, final Properties props) {
        this.farm = frm;
        this.properties = props;
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Response act(final Request req) throws IOException {
        final Bots bots = new Bots(
            this.farm.find("@id='PMO'").iterator().next()
        );
        bots.bootstrap();
        final String team = bots.register(
            new JdkRequest("https://slack.com/api/oauth.access")
                .uri()
                .queryParam(
                    "client_id",
                    this.properties.getProperty("slack.client_id")
                )
                .queryParam(
                    "client_secret",
                    this.properties.getProperty("slack.client_secret")
                )
                .queryParam(
                    "code",
                    new RqHref.Base(req).href()
                        .param("code").iterator().next()
                )
                .back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class)
                .json()
                .readObject()
        );
        return new RsWithStatus(
            new RsWithHeader(
                "Location",
                String.format(
                    "https://%s.slack.com/messages/@0crat/details/",
                    team
                )
            ),
            HttpURLConnection.HTTP_SEE_OTHER
        );
    }

}
