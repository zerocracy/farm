/**
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

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import com.zerocracy.tk.profile.TkAgenda;
import com.zerocracy.tk.profile.TkAwards;
import com.zerocracy.tk.profile.TkIdentify;
import com.zerocracy.tk.profile.TkKyc;
import com.zerocracy.tk.profile.TkProfile;
import com.zerocracy.tk.profile.TkYoti;
import com.zerocracy.tk.project.TkArchive;
import com.zerocracy.tk.project.TkArtifact;
import com.zerocracy.tk.project.TkBadge;
import com.zerocracy.tk.project.TkClaim;
import com.zerocracy.tk.project.TkDonate;
import com.zerocracy.tk.project.TkEquity;
import com.zerocracy.tk.project.TkFiles;
import com.zerocracy.tk.project.TkFootprint;
import com.zerocracy.tk.project.TkPay;
import com.zerocracy.tk.project.TkProject;
import com.zerocracy.tk.project.TkReport;
import com.zerocracy.tk.project.TkUpload;
import com.zerocracy.tk.project.TkXml;
import io.sentry.Sentry;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.apache.commons.text.StringEscapeUtils;
import org.cactoos.io.BytesOf;
import org.cactoos.list.SolidList;
import org.cactoos.text.TextOf;
import org.takes.Take;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.FbChain;
import org.takes.facets.fallback.FbLog4j;
import org.takes.facets.fallback.FbStatus;
import org.takes.facets.fallback.TkFallback;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.Fork;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.misc.Concat;
import org.takes.misc.Href;
import org.takes.misc.Opt;
import org.takes.rs.RsRedirect;
import org.takes.rs.RsText;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkText;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * Takes application.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle LineLength (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports" })
public final class TkApp extends TkWrap {

    /**
     * Ctor.
     * @param farm The farm
     * @param forks Additional forks
     * @throws IOException If fails
     */
    public TkApp(final Farm farm, final FkRegex... forks) throws IOException {
        this(farm, new Props(farm), forks);
    }

    /**
     * Ctor.
     * @param farm The farm
     * @param props Properties
     * @param forks Additional forks
     * @throws IOException If fails
     * @checkstyle MethodLengthCheck (500 lines)
     */
    public TkApp(final Farm farm, final Props props,
        final FkRegex... forks) throws IOException {
        super(
            new TkSslOnly(
                new TkFallback(
                    new TkWithHeaders(
                        new TkVersioned(
                            new TkMeasured(
                                new TkGzip(
                                    new TkFlash(
                                        new TkAppAuth(
                                            new TkForward(
                                                new TkFork(
                                                    new SolidList<Fork>(
                                                        new Concat<>(
                                                            new SolidList<>(forks),
                                                            new SolidList<>(
                                                                new FkRegex(
                                                                    "/\\.well-known/pki-validation/D6638B2C18C6793068D454E91E692397\\.txt",
                                                                    new TkText("30265BD04DBC892A0B22A97C81F04337B49CBBB18BE62476FEA4E78EC8C26FD4 comodoca.com 5a60937406a7f\n")
                                                                ),
                                                                new FkRegex("/", new TkIndex(farm)),
                                                                new FkRegex("/identify", new TkIdentify(farm)),
                                                                new FkRegex("/privacy", new TkRedirect("http://datum.zerocracy.com/pages/terms.html#privacy")),
                                                                new FkRegex("/yoti", new TkYoti(farm)),
                                                                new FkRegex("/heapdump", new TkDump(farm)),
                                                                new FkRegex("/guts", new TkGuts(farm)),
                                                                new FkRegex(
                                                                    "/org/takes/.+\\.xsl",
                                                                    new TkClasspath()
                                                                ),
                                                                new FkRegex("/ping", new TkPing(farm)),
                                                                new FkRegex("/robots.txt", ""),
                                                                new FkRegex(
                                                                    "/xsl/[a-z\\-]+\\.xsl",
                                                                    new TkWithType(
                                                                        new TkRefresh("./src/main/xsl"),
                                                                        "text/xsl"
                                                                    )
                                                                ),
                                                                new FkRegex(
                                                                    "/css/[a-z]+\\.css",
                                                                    new TkWithType(
                                                                        new TkRefresh("./src/main/scss"),
                                                                        "text/css"
                                                                    )
                                                                ),
                                                                new FkRegex(
                                                                    "/png/[a-z]+\\.png",
                                                                    new TkWithType(
                                                                        new TkClasspath(),
                                                                        "image/png"
                                                                    )
                                                                ),
                                                                new FkRegex(
                                                                    "/svg/[a-z]+\\.svg",
                                                                    new TkWithType(
                                                                        new TkClasspath(),
                                                                        "image/svg+xml"
                                                                    )
                                                                ),
                                                                new FkRegex(
                                                                    "/add_to_slack",
                                                                    new TkRedirect(
                                                                        new Href("https://slack.com/oauth/authorize")
                                                                            .with("scope", "bot")
                                                                            .with("client_id", props.get("//slack/client_id", ""))
                                                                            .toString()
                                                                    )
                                                                ),
                                                                new FkRegex("/board", new TkBoard(farm)),
                                                                new FkRegex("/gang", new TkGang(farm)),
                                                                new FkRegex(
                                                                    "/me",
                                                                    (Take) req -> new RsRedirect(
                                                                        String.format("/u/%s", new RqUser(farm, req).value())
                                                                    )
                                                                ),
                                                                new FkRegex(
                                                                    "/badge/(PMO|[A-Z0-9]{9})\\.svg",
                                                                    new TkBadge()
                                                                ),
                                                                new FkRegex(
                                                                    "/p/(PMO|[A-Z0-9]{9})",
                                                                    new TkProject(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/footprint/(PMO|[A-Z0-9]{9})",
                                                                    new TkFootprint(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/footprint/([A-Z0-9]{9})/([A-Zaz0-9]+)",
                                                                    new TkClaim(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/report/(PMO|[A-Z0-9]{9})",
                                                                    new TkReport(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/files/(PMO|[A-Z0-9]{9})",
                                                                    new TkFiles(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/upload/(PMO|[A-Z0-9]{9})",
                                                                    new TkUpload(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/archive/(PMO|[A-Z0-9]{9})",
                                                                    new TkArchive(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/equity/([A-Z0-9]{9})",
                                                                    new TkEquity(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/pay/(PMO|[A-Z0-9]{9})",
                                                                    new TkPay(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/donate/(PMO|[A-Z0-9]{9})",
                                                                    new TkDonate(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/a/(PMO|[A-Z0-9]{9})",
                                                                    new TkArtifact(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/xml/(PMO|[A-Z0-9]{9})",
                                                                    new TkXml(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/kyc/([a-zA-Z0-9-]+)",
                                                                    new TkKyc(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/u/([a-zA-Z0-9-]+)/awards",
                                                                    new TkAwards(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/u/([a-zA-Z0-9-]+)/agenda",
                                                                    new TkAgenda(farm)
                                                                ),
                                                                new FkRegex(
                                                                    "/u/([a-zA-Z0-9-]+)",
                                                                    new TkProfile(farm)
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            ),
                                            farm
                                        )
                                    )
                                )
                            )
                        ),
                        String.format(
                            "X-Zerocracy-Version: %s %s %s",
                            props.get("//build/version", ""),
                            props.get("//build/revision", ""),
                            props.get("//build/date", "")
                        ),
                        "Vary: Cookie"
                    ),
                    new FbChain(
                        new FbStatus(
                            HttpURLConnection.HTTP_NOT_FOUND,
                            (Fallback) req -> new Opt.Single<>(
                                new RsWithStatus(
                                    new RsText(req.throwable().getMessage()),
                                    req.code()
                                )
                            )
                        ),
                        req -> {
                            Logger.error(req, "%[exception]s", req.throwable());
                            return new Opt.Empty<>();
                        },
                        new FbLog4j(),
                        req -> {
                            Sentry.capture(req.throwable());
                            return new Opt.Empty<>();
                        },
                        req -> new Opt.Single<>(
                            new RsWithStatus(
                                new RsWithType(
                                    new RsVelocity(
                                        TkApp.class.getResource("error.html.vm"),
                                        new RsVelocity.Pair(
                                            "error",
                                            StringEscapeUtils.escapeHtml4(
                                                new TextOf(
                                                    new BytesOf(
                                                        req.throwable()
                                                    )
                                                ).asString()
                                            )
                                        ),
                                        new RsVelocity.Pair(
                                            "version",
                                            props.get("//build/version", "")
                                        ),
                                        new RsVelocity.Pair(
                                            "revision",
                                            props.get("//build/revision", "")
                                        )
                                    ),
                                    "text/html"
                                ),
                                HttpURLConnection.HTTP_INTERNAL_ERROR
                            )
                        )
                    )
                )
            )
        );
    }

}
