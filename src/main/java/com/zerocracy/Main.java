/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy;

import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.Protocol;
import com.jcabi.email.Token;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.enclosure.EnPlain;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.wire.SMTP;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.s3.Region;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.StkSafe;
import com.zerocracy.farm.reactive.Brigade;
import com.zerocracy.farm.reactive.RvFarm;
import com.zerocracy.farm.reactive.StkGroovy;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.radars.github.GhookRadar;
import com.zerocracy.radars.github.GithubFetch;
import com.zerocracy.radars.github.RbByActions;
import com.zerocracy.radars.github.RbLogged;
import com.zerocracy.radars.github.RbOnClose;
import com.zerocracy.radars.github.RbOnComment;
import com.zerocracy.radars.github.RbPingArchitect;
import com.zerocracy.radars.github.RbTweet;
import com.zerocracy.radars.github.ReOnComment;
import com.zerocracy.radars.github.ReOnInvitation;
import com.zerocracy.radars.github.ReOnReason;
import com.zerocracy.radars.github.ReQuestion;
import com.zerocracy.radars.github.Reaction;
import com.zerocracy.radars.github.Rebound;
import com.zerocracy.radars.slack.ReIfAddressed;
import com.zerocracy.radars.slack.ReIfDirect;
import com.zerocracy.radars.slack.ReLogged;
import com.zerocracy.radars.slack.ReNotMine;
import com.zerocracy.radars.slack.ReProfile;
import com.zerocracy.radars.slack.ReProject;
import com.zerocracy.radars.slack.ReSafe;
import com.zerocracy.radars.slack.SlackRadar;
import com.zerocracy.tk.TkAlias;
import com.zerocracy.tk.TkApp;
import com.zerocracy.tk.TkPing;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.takes.facets.fork.FkRegex;
import org.takes.http.Exit;
import org.takes.http.FtCli;

/**
 * Main entry point.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle LineLengthCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @checkstyle MethodLengthCheck (500 lines)
 */
@SuppressWarnings(
    {
        "PMD.ExcessiveImports",
        "PMD.ExcessiveMethodLength",
        "PMD.AvoidDuplicateLiterals"
    }
)
public final class Main {

    /**
     * Command line args.
     */
    private final String[] arguments;

    /**
     * Ctor.
     * @param args Command line arguments
     */
    public Main(final String... args) {
        this.arguments = args;
    }

    /**
     * Main.
     * @param args Command line arguments
     * @throws IOException If fails on I/O
     */
    public static void main(final String... args) throws IOException {
        new Main(args).exec();
    }

    /**
     * Run it.
     * @throws IOException If fails on I/O
     */
    public void exec() throws IOException {
        final Properties props = new Properties();
        try (final InputStream input =
            this.getClass().getResourceAsStream("/main.properties")) {
            props.load(input);
        }
        final Github github = new RtGithub(
            props.getProperty("github.0crat.login"),
            props.getProperty("github.0crat.password")
        );
        final Map<String, SlackSession> sessions = new ConcurrentHashMap<>(0);
        final Stakeholder[] stakeholders = {
            new StkGroovy("hello.groovy"),
            new StkGroovy(
                "version.groovy",
                new StkGroovy.Pair(
                    "version", props.getProperty("build.version")
                ),
                new StkGroovy.Pair(
                    "revision", props.getProperty("build.revision")
                ),
                new StkGroovy.Pair(
                    "date", props.getProperty("build.date")
                )
            ),
            new StkGroovy(
                "pmo/links/add_link.groovy",
                new StkGroovy.Pair("github", github)
            ),
            new StkGroovy("pmo/links/remove_link.groovy"),
            new StkGroovy("pmo/links/show_all_links.groovy"),
            new StkGroovy("pmo/profile/invite_friend.groovy"),
            new StkGroovy("pmo/profile/aliases/show_aliases.groovy"),
            new StkGroovy("pmo/profile/rate/set_rate.groovy"),
            new StkGroovy("pmo/profile/rate/show_rate.groovy"),
            new StkGroovy("pmo/profile/skills/add_skills.groovy"),
            new StkGroovy("pmo/profile/skills/show_skills.groovy"),
            new StkGroovy("pmo/profile/wallet/set_wallet.groovy"),
            new StkGroovy("pmo/profile/wallet/show_wallet.groovy"),
            new StkGroovy("pmo/set_parent.groovy"),
            new StkGroovy("pm/bootstrap.groovy"),
            new StkGroovy("pm/comm/notify.groovy"),
            new StkGroovy(
                "pm/comm/notify_in_slack.groovy",
                new StkGroovy.Pair("sessions", sessions)
            ),
            new StkGroovy(
                "pm/comm/notify_in_github.groovy",
                new StkGroovy.Pair("github", github)
            ),
            new StkGroovy("pm/hr/roles/show_all_roles.groovy"),
            new StkGroovy("pm/hr/roles/assign_role.groovy"),
            new StkGroovy("pm/hr/roles/resign_role.groovy"),
            new StkGroovy(
                "pm/hr/roles/follow_in_github.groovy",
                new StkGroovy.Pair("github", github)
            ),
            new StkGroovy("pm/in/orders/confide_performer.groovy"),
            new StkGroovy("pm/in/orders/start_order.groovy"),
            new StkGroovy("pm/in/orders/stop_order.groovy"),
            new StkGroovy(
                "pm/in/orders/set_assignee.groovy",
                new StkGroovy.Pair("github", github)
            ),
            new StkGroovy(
                "pm/in/orders/remove_assignee.groovy",
                new StkGroovy.Pair("github", github)
            ),
            new StkGroovy("pm/scope/wbs/add_job_to_wbs.groovy"),
            new StkGroovy("pm/scope/wbs/remove_job_from_wbs.groovy"),
            new StkGroovy("pm/scope/wbs/show_wbs.groovy"),
        };
        final Farm farm = new RvFarm(
            new SyncFarm(
                new S3Farm(
                    new com.jcabi.s3.retry.ReRegion(
                        new Region.Simple(
                            props.getProperty("s3.key"),
                            props.getProperty("s3.secret")
                        )
                    ).bucket(props.getProperty("s3.bucket"))
                )
            ),
            new Brigade(
                Arrays.stream(stakeholders)
                    .map(StkSafe::new)
                    .collect(Collectors.toList())
            ),
            Executors.newSingleThreadExecutor(new Main.MailThreads(props))
        );
        final SlackRadar skradar = new SlackRadar(
            farm, props, sessions,
            new ReSafe(
                new ReLogged<>(
                    new ReNotMine(
                        new ReIfDirect(
                            new ReProfile(),
                            new ReIfAddressed(
                                new ReProject()
                            )
                        )
                    )
                )
            )
        );
        final com.jcabi.dynamo.Region dynamo = new ReRegion(
            new com.jcabi.dynamo.Region.Simple(
                new Credentials.Simple(
                    props.getProperty("dynamo.key"),
                    props.getProperty("dynamo.secret")
                )
            )
        );
        try {
            final GhookRadar gkradar = new GhookRadar(
                farm, github,
                new RbLogged(
                    new Rebound.Chain(
                        new RbByActions(
                            new RbOnComment(
                                new GithubFetch(
                                    farm,
                                    github,
                                    new com.zerocracy.radars.github.ReLogged(
                                        new Reaction.Chain(
                                            new ReOnReason("invitation", new ReOnInvitation(github)),
                                            new ReOnReason(
                                                "mention",
                                                new ReOnComment(
                                                    github,
                                                    new com.zerocracy.radars.github.ReSafe(
                                                        new com.zerocracy.radars.github.ReNotMine(
                                                            new com.zerocracy.radars.github.ReIfAddressed(
                                                                new ReQuestion()
                                                            )
                                                        )
                                                    ),
                                                    dynamo.table("0crat-github")
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            "created"
                        ),
                        new RbByActions(
                            new RbPingArchitect(),
                            "opened", "reopened"
                        ),
                        new RbByActions(
                            new RbOnClose(),
                            "closed"
                        ),
                        new RbTweet(
                            dynamo.table("0crat-tweets"),
                            props.getProperty("twitter.key"),
                            props.getProperty("twitter.secret"),
                            props.getProperty("twitter.token"),
                            props.getProperty("twitter.tsecret")
                        )
                    )
                )
            );
            skradar.start();
            new FtCli(
                new TkApp(
                    props,
                    new FkRegex("/slack", skradar),
                    new FkRegex("/alias", new TkAlias(farm)),
                    new FkRegex("/ghook", gkradar),
                    new FkRegex("/ping", new TkPing(farm))
                ),
                this.arguments
            ).start(Exit.NEVER);
        } finally {
            skradar.close();
        }
    }

    /**
     * Factory with threads that mail all exceptions.
     */
    private static class MailThreads implements ThreadFactory {
        /**
         * Original factory.
         */
        private final ThreadFactory origin;
        /**
         * Props.
         */
        private final Properties props;
        /**
         * Ctor.
         * @param pps Props
         */
        MailThreads(final Properties pps) {
            this.props = pps;
            this.origin = new VerboseThreads();
        }
        @Override
        @SuppressWarnings("PMD.AvoidCatchingThrowable")
        public Thread newThread(final Runnable runnable) {
            return this.origin.newThread(
                new VerboseRunnable(
                    () -> {
                        try {
                            runnable.run();
                            // @checkstyle IllegalCatchCheck (1 line)
                        } catch (final Throwable ex) {
                            this.mail(ex);
                            throw ex;
                        }
                    },
                    true, true
                )
            );
        }
        /**
         * Send this error by email.
         * @param error The error
         */
        private void mail(final Throwable error) {
            final Postman postman = new Postman.Default(
                new SMTP(
                    new Token(
                        this.props.getProperty("smtp.username"),
                        this.props.getProperty("smtp.password")
                    ).access(
                        new Protocol.SMTP(
                            this.props.getProperty("smtp.host"),
                            Integer.parseInt(this.props.getProperty("smtp.port"))
                        )
                    )
                )
            );
            try {
                postman.send(
                    new Envelope.MIME()
                        .with(new StSender("0crat <no-reply@0crat.com>"))
                        .with(new StRecipient("0crat admin <bugs@0crat.com>"))
                        .with(new StSubject(error.getLocalizedMessage()))
                        .with(
                            new EnPlain(
                                String.format(
                                    "Hi,\n\n%s\n\n--\n0crat\n%s %s %s",
                                    ExceptionUtils.getStackTrace(error),
                                    this.props.getProperty("build.version"),
                                    this.props.getProperty("build.revision"),
                                    this.props.getProperty("build.date")
                                )
                            )
                        )
                        .with(
                            new EnHTML(
                                String.format(
                                    "<html><body><p>Hi,</p><p>There was a problem:</p><pre>%s</pre><p>--<br/>0crat<br/>%s %s %s</p></body></html>",
                                    ExceptionUtils.getStackTrace(error),
                                    this.props.getProperty("build.version"),
                                    this.props.getProperty("build.revision"),
                                    this.props.getProperty("build.date")
                                )
                            )
                        )
                );
            } catch (final IOException ioex) {
                throw new IllegalStateException(ioex);
            }
        }
    }

}
