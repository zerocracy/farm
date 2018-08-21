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

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.farm.guts.TkGuts;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.Exam;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tk.profile.TkAgenda;
import com.zerocracy.tk.profile.TkAwards;
import com.zerocracy.tk.profile.TkIdentify;
import com.zerocracy.tk.profile.TkKyc;
import com.zerocracy.tk.profile.TkProfile;
import com.zerocracy.tk.profile.TkUserWbs;
import com.zerocracy.tk.profile.TkYoti;
import com.zerocracy.tk.project.RqProject;
import com.zerocracy.tk.project.TkArchive;
import com.zerocracy.tk.project.TkArtifact;
import com.zerocracy.tk.project.TkBadge;
import com.zerocracy.tk.project.TkClaim;
import com.zerocracy.tk.project.TkContrib;
import com.zerocracy.tk.project.TkContribBadge;
import com.zerocracy.tk.project.TkContribLedger;
import com.zerocracy.tk.project.TkContribPay;
import com.zerocracy.tk.project.TkDonate;
import com.zerocracy.tk.project.TkEquity;
import com.zerocracy.tk.project.TkFiles;
import com.zerocracy.tk.project.TkFootprint;
import com.zerocracy.tk.project.TkHiring;
import com.zerocracy.tk.project.TkProject;
import com.zerocracy.tk.project.TkReport;
import com.zerocracy.tk.project.TkStripePay;
import com.zerocracy.tk.project.TkUpload;
import com.zerocracy.tk.project.TkXml;
import com.zerocracy.tk.rfp.TkPrepay;
import com.zerocracy.tk.rfp.TkRfp;
import com.zerocracy.tk.rfp.TkRfps;
import com.zerocracy.tk.rfp.TkSubmit;
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
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.TkForward;
import org.takes.misc.Concat;
import org.takes.misc.Href;
import org.takes.misc.Opt;
import org.takes.rs.RsRedirect;
import org.takes.rs.RsText;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.rs.xe.XeAppend;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkSslOnly;
import org.takes.tk.TkText;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * Takes application.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle LineLength (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"})
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
                                                new TkSoftForward(
                                                    new TkFork(
                                                        new SolidList<Fork>(
                                                            new Concat<Fork>(
                                                                new SolidList<>(forks),
                                                                new SolidList<>(
                                                                    new FkRegex(
                                                                        "/\\.well-known/pki-validation/D6638B2C18C6793068D454E91E692397\\.txt",
                                                                        new TkText("30265BD04DBC892A0B22A97C81F04337B49CBBB18BE62476FEA4E78EC8C26FD4 comodoca.com 5a60937406a7f\n")
                                                                    ),
                                                                    new FkRegex("/", new TkIndex(farm)),
                                                                    new FkRegex("/pulse", new TkPulse(farm)),
                                                                    new FkRegex(
                                                                        "/home",
                                                                        (TkRegex) req -> new RsRedirect(
                                                                            String.format("/u/%s", new RqUser(farm, req, false).value())
                                                                        )
                                                                    ),
                                                                    new FkRegex("/identify", new TkIdentify(farm)),
                                                                    new FkRegex("/privacy", new TkRedirect("http://www.zerocracy.com/terms.html#privacy")),
                                                                    new FkRegex("/yoti", new TkYoti(farm)),
                                                                    new FkRegex("/heapdump", new TkDump()),
                                                                    new FkRegex("/guts", new TkGuts(farm)),
                                                                    new FkRegex(
                                                                        "/spam",
                                                                        (Take) req -> new RsPage(
                                                                            farm, "/xsl/spam.xsl", req
                                                                        )
                                                                    ),
                                                                    new FkRegex("/spam-send", new TkSpam(farm)),
                                                                    new FkRegex("/shutdown", new TkShutdown(props, farm)),
                                                                    new FkRegex("/policy", new TkPolicy()),
                                                                    new FkRegex("/join", new TkJoin(farm)),
                                                                    new FkRegex("/join-post", new TkJoinPost(farm)),
                                                                    new FkRegex(
                                                                        "/org/takes/.+\\.xsl",
                                                                        new TkClasspath()
                                                                    ),
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
                                                                        "/png/[a-z\\-]+\\.png",
                                                                        new TkWithType(
                                                                            new TkClasspath(),
                                                                            "image/png"
                                                                        )
                                                                    ),
                                                                    new FkRegex(
                                                                        "/svg/[a-z\\-]+\\.svg",
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
                                                                    new FkRegex("/rfp", new TkRfp(farm)),
                                                                    new FkRegex("/rfps", new TkRfps(farm)),
                                                                    new FkRegex("/rfp-pay", new TkPrepay(farm)),
                                                                    new FkRegex("/rfp-post", new TkSubmit(farm)),
                                                                    new FkRegex("/board", new TkBoard(farm)),
                                                                    new FkRegex("/team", new TkTeam(farm)),
                                                                    new FkRegex("/gang", new TkRedirect("/team")),
                                                                    new FkRegex("/me", new TkRedirect("/home")),
                                                                    new FkRegex("/vacancies", new TkVacancies(farm)),
                                                                    new FkRegex(
                                                                        "/p/(PMO|[A-Z0-9]{9})",
                                                                        new TkProject(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/hiring/([A-Z0-9]{9})",
                                                                        (TkRegex) req -> {
                                                                            new Exam(farm, new RqUser(farm, req, false).value()).min("51.min", 1024);
                                                                            return new RsPage(
                                                                                farm, "/xsl/hiring.xsl", req,
                                                                                () -> new XeAppend(
                                                                                    "project",
                                                                                    new RqProject(farm, req, "PO", "ARC").pid()
                                                                                )
                                                                            );
                                                                        }
                                                                    ),
                                                                    new FkRegex(
                                                                        "/badge/([A-Z0-9]{9})\\.svg",
                                                                        new TkBadge(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/contrib-badge/([A-Z0-9]{9})\\.svg",
                                                                        new TkContribBadge(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/hiring-send/([A-Z0-9]{9})",
                                                                        new TkHiring(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/contrib/([A-Z0-9]{9})",
                                                                        new TkContrib(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/contrib-pay/([A-Z0-9]{9})",
                                                                        new TkContribPay(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/contrib-ledger/([A-Z0-9]{9})",
                                                                        new TkContribLedger(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/footprint/(PMO|[A-Z0-9]{9})",
                                                                        new TkFootprint(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/footprint/(PMO|[A-Z0-9]{9})/([0-9]+)",
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
                                                                        new TkStripePay(farm)
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
                                                                        "/u/([a-zA-Z0-9-]+)/wbs",
                                                                        new TkUserWbs(farm)
                                                                    ),
                                                                    new FkRegex(
                                                                        "/u/([a-zA-Z0-9-]+)",
                                                                        new TkProfile(farm)
                                                                    )
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
                            new SafeSentry(farm).capture(req.throwable());
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
