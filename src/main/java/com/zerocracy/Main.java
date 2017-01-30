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
import com.zerocracy.radars.github.GhookRadar;
import com.zerocracy.radars.github.GithubFetch;
import com.zerocracy.radars.github.RbByActions;
import com.zerocracy.radars.github.RbLogged;
import com.zerocracy.radars.github.RbOnClose;
import com.zerocracy.radars.github.RbOnComment;
import com.zerocracy.radars.github.RbPingArchitect;
import com.zerocracy.radars.github.ReOnComment;
import com.zerocracy.radars.github.ReOnInvitation;
import com.zerocracy.radars.github.ReOnReason;
import com.zerocracy.radars.github.ReQuestion;
import com.zerocracy.radars.github.ReRegex;
import com.zerocracy.radars.github.Rebound;
import com.zerocracy.radars.github.Response;
import com.zerocracy.radars.github.StkNotify;
import com.zerocracy.radars.slack.ReIfAddressed;
import com.zerocracy.radars.slack.ReIfDirect;
import com.zerocracy.radars.slack.ReLogged;
import com.zerocracy.radars.slack.ReNotMine;
import com.zerocracy.radars.slack.ReProfile;
import com.zerocracy.radars.slack.ReProject;
import com.zerocracy.radars.slack.ReSafe;
import com.zerocracy.radars.slack.ReSay;
import com.zerocracy.radars.slack.Reaction;
import com.zerocracy.radars.slack.SlackRadar;
import com.zerocracy.stk.StkByRoles;
import com.zerocracy.stk.StkSafe;
import com.zerocracy.stk.StkTrashBin;
import com.zerocracy.stk.StkVerbose;
import com.zerocracy.stk.pm.hr.roles.StkAssign;
import com.zerocracy.stk.pm.hr.roles.StkResign;
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
import java.util.Arrays;
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
                        new Region.Simple(
                            props.getProperty("s3.key"),
                            props.getProperty("s3.secret")
                        ).bucket(props.getProperty("s3.bucket"))
                    )
                ),
                Stream
                    .of(
                        new StkNotify(github),
                        new com.zerocracy.radars.slack.StkNotify(sessions),
                        new StkByRoles(new StkAdd(github), "PO"),
                        new StkByRoles(new StkRemove(), "PO"),
                        new StkByRoles(new StkShow(), "PO"),
                        new StkByRoles(new StkParent(), "PO"),
                        new StkSet(),
                        new StkByRoles(new com.zerocracy.stk.pm.hr.roles.StkShow(), "PO", "ARC"),
                        new StkByRoles(new StkAssign(github), "PO"),
                        new StkByRoles(new StkResign(), "PO"),
                        new StkByRoles(new com.zerocracy.stk.pm.scope.wbs.StkShow(), "PO", "ARC"),
                        new StkByRoles(new StkInto(), "PO", "ARC"),
                        new StkByRoles(new StkOut(), "PO", "ARC"),
                        new com.zerocracy.stk.pmo.profile.rate.StkShow(),
                        new com.zerocracy.stk.pmo.profile.wallet.StkSet(),
                        new com.zerocracy.stk.pmo.profile.wallet.StkShow(),
                        new com.zerocracy.stk.pmo.profile.skills.StkAdd(),
                        new com.zerocracy.stk.pmo.profile.skills.StkShow(),
                        new com.zerocracy.stk.pmo.profile.aliases.StkShow(),
                        new StkTrashBin("type='ping'")
                    )
                    .map(StkVerbose::new)
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
                            new Reaction.Chain<>(
                                Arrays.asList(
                                    new com.zerocracy.radars.slack.ReRegex("hi|hello|hey", new ReSay("Hey, how is it going?")),
                                    new ReProfile()
                                )
                            ),
                            new ReIfAddressed(
                                new Reaction.Chain<>(
                                    Arrays.asList(
                                        new com.zerocracy.radars.slack.ReRegex("hello|hi|hey", new ReSay("What's up?")),
                                        new ReProject()
                                    )
                                )
                            )
                        )
                    )
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
                                        new com.zerocracy.radars.github.Reaction.Chain(
                                            new ReOnReason("invitation", new ReOnInvitation(github)),
                                            new ReOnReason(
                                                "mention",
                                                new ReOnComment(
                                                    github,
                                                    new com.zerocracy.radars.github.ReSafe(
                                                        new com.zerocracy.radars.github.ReNotMine(
                                                            new com.zerocracy.radars.github.ReIfAddressed(
                                                                new Response.Chain(
                                                                    new ReRegex("hello|hey|hi|morning", new com.zerocracy.radars.github.ReSay("Hey, I'm here, what's up?")),
                                                                    new ReQuestion()
                                                                )
                                                            )
                                                        )
                                                    ),
                                                    new ReRegion(
                                                        new com.jcabi.dynamo.Region.Simple(
                                                            new Credentials.Simple(
                                                                props.getProperty("dynamo.key"),
                                                                props.getProperty("dynamo.secret")
                                                            )
                                                        )
                                                    ).table("0crat-github")
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
