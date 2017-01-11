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
package com.zerocracy.farm;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pmo.Bots;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqHref;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithStatus;
import org.takes.tk.TkWrap;

/**
 * Takes application.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class TkApp extends TkWrap {

    /**
     * When we started.
     */
    private static final long STARTED = System.currentTimeMillis();

    /**
     * Ctor.
     * @param farm Farm
     * @param version App version
     * @param sid Slack client_id
     * @param secret Slack client_secret
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    TkApp(final Farm farm, final String version, final String sid,
        final String secret) {
        super(TkApp.make(farm, version, sid, secret));
    }

    /**
     * Ctor.
     * @param farm Farm
     * @param version App version
     * @param sid Slack client_id
     * @param secret Slack client_secret
     * @return Takes
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private static Take make(final Farm farm, final String version,
        final String sid, final String secret) {
        return new TkFork(
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/",
                (Take) req -> new RsVelocity(
                    TkApp.class.getResource("/html/index.html"),
                    new RsVelocity.Pair("version", version),
                    new RsVelocity.Pair(
                        "alive",
                        Logger.format(
                            "%[ms]s",
                            System.currentTimeMillis() - TkApp.STARTED
                        )
                    )
                )
            ),
            new FkRegex(
                "/slack",
                (Take) req -> {
                    final Bots bots = new Bots(
                        farm.find("@id='PMO'").iterator().next()
                    );
                    bots.bootstrap();
                    bots.register(
                        new JdkRequest("https://slack.com/api/oauth.access")
                            .uri()
                            .queryParam("client_id", sid)
                            .queryParam("client_secret", secret)
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
                            "http://www.zerocracy.com"
                        ),
                        HttpURLConnection.HTTP_SEE_OTHER
                    );
                }
            ),
            new FkRegex(
                "/([a-z\\-]+)\\.html",
                (TkRegex) req -> new RsVelocity(
                    TkApp.class.getResource("/layout/page.html"),
                    new RsVelocity.Pair("title", req.matcher().group(1)),
                    new RsVelocity.Pair(
                        "html",
                        Processor.process(
                            IOUtils.toString(
                                TkApp.class.getResource(
                                    String.format(
                                        "/pages/%s.md",
                                        req.matcher().group(1)
                                    )
                                ),
                                StandardCharsets.UTF_8
                            ),
                            Configuration.DEFAULT_SAFE
                        )
                    )
                )
            )
        );
    }

}
