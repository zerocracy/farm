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
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.s3.Region;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.zerocracy.farm.PingFarm;
import com.zerocracy.farm.ReactiveFarm;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.SyncFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.fake.FkStakeholder;
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
import com.zerocracy.stk.StkByTerm;
import com.zerocracy.stk.StkHello;
import com.zerocracy.stk.StkSafe;
import com.zerocracy.stk.StkWipe;
import com.zerocracy.stk.TmOr;
import com.zerocracy.stk.TmRoles;
import com.zerocracy.stk.TmType;
import com.zerocracy.stk.TmXpath;
import com.zerocracy.stk.github.StkNotify;
import com.zerocracy.stk.github.StkRemoveAssignee;
import com.zerocracy.stk.github.StkSetAssignee;
import com.zerocracy.stk.pm.StkBootstrap;
import com.zerocracy.stk.pm.hr.roles.StkAssign;
import com.zerocracy.stk.pm.hr.roles.StkResign;
import com.zerocracy.stk.pm.in.orders.StkConfide;
import com.zerocracy.stk.pm.in.orders.StkStart;
import com.zerocracy.stk.pm.in.orders.StkStarted;
import com.zerocracy.stk.pm.in.orders.StkStop;
import com.zerocracy.stk.pm.scope.wbs.StkInto;
import com.zerocracy.stk.pm.scope.wbs.StkOut;
import com.zerocracy.stk.pmo.StkParent;
import com.zerocracy.stk.pmo.links.StkAdd;
import com.zerocracy.stk.pmo.links.StkRemove;
import com.zerocracy.stk.pmo.links.StkShow;
import com.zerocracy.stk.pmo.profile.rate.StkSet;
import com.zerocracy.tk.TkAlias;
import com.zerocracy.tk.TkApp;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        final Farm farm = new PingFarm(
            new ReactiveFarm(
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
                Stream
                    .of(
                        new StkByTerm(
                            new StkNotify(github),
                            new TmXpath("type='notify' and starts-with(token, 'github;')")
                        ),
                        new StkByTerm(
                            new com.zerocracy.stk.slack.StkNotify(sessions),
                            new TmXpath("type='notify' and starts-with(token, 'slack;')")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkHello()),
                            new TmType("hello")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkBootstrap()),
                            new TmType("pm.bootstrap")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkAdd(github)),
                            new TmType("pmo.links.add"),
                            new TmRoles("PO")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkRemove()),
                            new TmType("pmo.links.remove"),
                            new TmRoles("PO")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkShow()),
                            new TmType("pmo.links.show"),
                            new TmRoles("PO")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkParent()),
                            new TmType("pmo.parent.set"),
                            new TmRoles("PO")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkSet()),
                            new TmType("pmo.profile.rate.set")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pmo.profile.rate.StkShow()),
                            new TmType("pmo.profile.rate.show")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pmo.profile.wallet.StkSet()),
                            new TmType("pmo.profile.wallet.set")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pmo.profile.wallet.StkShow()),
                            new TmType("pmo.profile.wallet.show")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pmo.profile.skills.StkAdd()),
                            new TmType("pmo.profile.skills.add")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pmo.profile.skills.StkShow()),
                            new TmType("pmo.profile.skills.show")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pmo.profile.aliases.StkShow()),
                            new TmType("pmo.profile.aliases.show")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkAssign(github)),
                            new TmType("pm.hr.roles.add"),
                            new TmRoles("PO")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkResign()),
                            new TmType("pm.hr.roles.remove"),
                            new TmRoles("PO")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pm.hr.roles.StkShow()),
                            new TmType("pm.hr.roles.show"),
                            new TmRoles("PO", "ARC")
                        ),
                        new StkByTerm(
                            new StkWipe(new com.zerocracy.stk.pm.scope.wbs.StkShow()),
                            new TmType("pm.scope.wbs.show"),
                            new TmRoles("PO", "ARC")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkInto()),
                            new TmType("pm.scope.wbs.in"),
                            new TmRoles("PO", "ARC")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkOut()),
                            new TmType("pm.scope.wbs.out"),
                            new TmOr(
                                new TmXpath("not(author)"),
                                new TmRoles("PO", "ARC")
                            )
                        ),
                        new StkByTerm(
                            new StkConfide(),
                            new TmOr(
                                new TmType("ping"),
                                new TmType("pm.scope.wbs.added")
                            )
                        ),
                        new StkByTerm(
                            new StkWipe(new StkStart()),
                            new TmType("pm.in.orders.start"),
                            new TmRoles("PO", "ARC")
                        ),
                        new StkByTerm(
                            new StkStarted(),
                            new TmType("pm.in.orders.started")
                        ),
                        new StkByTerm(
                            new StkSetAssignee(github),
                            new TmType("pm.in.orders.started"),
                            new TmXpath("params/param[@name='job' and starts-with(.,'gh:')]")
                        ),
                        new StkByTerm(
                            new StkWipe(new StkStop()),
                            new TmType("pm.in.orders.stop")
                        ),
                        new StkByTerm(
                            new StkRemoveAssignee(github),
                            new TmType("pm.in.orders.stopped"),
                            new TmXpath("params/param[@name='job' and starts-with(.,'gh:')]")
                        ),
                        new StkByTerm(
                            new StkWipe(new FkStakeholder()),
                            new TmOr(
                                new TmType("pm.scope.wbs.added"),
                                new TmType("pm.scope.wbs.removed"),
                                new TmType("pm.in.orders.started"),
                                new TmType("pm.in.orders.stopped")
                            )
                        )
                    )
                    .map(StkSafe::new)
                    .collect(Collectors.toList())
            )
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
                    new FkRegex("/ghook", gkradar)
                ),
                this.arguments
            ).start(Exit.NEVER);
        } finally {
            skradar.close();
        }
    }

}
