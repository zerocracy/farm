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

import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.s3.Region;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.zerocracy.farm.PingFarm;
import com.zerocracy.farm.ReactiveFarm;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.SyncFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.radars.Radar;
import com.zerocracy.radars.ghook.GhookRadar;
import com.zerocracy.radars.github.GithubRadar;
import com.zerocracy.radars.github.ReOnComment;
import com.zerocracy.radars.github.ReOnInvitation;
import com.zerocracy.radars.github.ReOnReason;
import com.zerocracy.radars.github.ReQuestion;
import com.zerocracy.radars.github.ReRegex;
import com.zerocracy.radars.github.Response;
import com.zerocracy.radars.github.StkNotify;
import com.zerocracy.radars.slack.ReHello;
import com.zerocracy.radars.slack.ReIfAddressed;
import com.zerocracy.radars.slack.ReIfDirect;
import com.zerocracy.radars.slack.ReLogged;
import com.zerocracy.radars.slack.ReNotMine;
import com.zerocracy.radars.slack.RePmo;
import com.zerocracy.radars.slack.ReProfile;
import com.zerocracy.radars.slack.ReProject;
import com.zerocracy.radars.slack.ReSafe;
import com.zerocracy.radars.slack.ReSorry;
import com.zerocracy.radars.slack.Reaction;
import com.zerocracy.radars.slack.SlackRadar;
import com.zerocracy.stk.StkSafe;
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
@SuppressWarnings("PMD.ExcessiveImports")
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
                Stream.of(
                    new StkNotify(github),
                    new com.zerocracy.radars.slack.StkNotify(sessions),
                    new StkAdd(github),
                    new StkRemove(),
                    new StkShow(),
                    new StkParent(),
                    new StkSet(),
                    new com.zerocracy.stk.pmo.profile.rate.StkShow(),
                    new com.zerocracy.stk.pmo.profile.wallet.StkSet(),
                    new com.zerocracy.stk.pmo.profile.wallet.StkShow(),
                    new com.zerocracy.stk.pmo.profile.skills.StkAdd(),
                    new com.zerocracy.stk.pmo.profile.skills.StkShow(),
                    new com.zerocracy.stk.pmo.profile.aliases.StkShow()
                ).map(StkSafe::new).collect(Collectors.toList())
            )
        );
        final GithubRadar ghradar = Main.ghradar(farm, github);
        final SlackRadar skradar = Main.skradar(farm, props, sessions);
        try (final Radar chain = new Radar.Chain(ghradar, skradar)) {
            final GhookRadar gkradar = Main.gkradar(farm, github);
            chain.start();
            new FtCli(
                new TkApp(
                    props,
                    new FkRegex("/slack", skradar),
                    new FkRegex("/alias", new TkAlias(farm)),
                    new FkRegex("/ghook", gkradar)
                ),
                this.arguments
            ).start(Exit.NEVER);
        }
    }

    /**
     * Github radar.
     * @param farm Farm
     * @param github Github client
     * @return Radar
     */
    private static GithubRadar ghradar(final Farm farm, final Github github) {
        return new GithubRadar(
            farm,
            github,
            new com.zerocracy.radars.github.ReLogged(
                new com.zerocracy.radars.github.Reaction.Chain(
                    new ReOnReason("invitation", new ReOnInvitation(github)),
                    new ReOnReason(
                        "mention",
                        new ReOnComment(
                            github,
                            new com.zerocracy.radars.github.ReNotMine(
                                new Response.Chain(
                                    new ReRegex("hello", new com.zerocracy.radars.github.ReHello()),
                                    new ReQuestion(),
                                    new ReRegex(".*", new com.zerocracy.radars.github.ReSorry())
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Ghook radar.
     * @param farm Farm
     * @param github Github client
     * @return Radar
     */
    private static GhookRadar gkradar(final Farm farm, final Github github) {
        return new GhookRadar(farm, github);
    }

    /**
     * Slack radar.
     * @param farm Farm
     * @param props Props
     * @param sessions Sessions
     * @return Radar
     */
    private static SlackRadar skradar(final Farm farm, final Properties props,
        final Map<String, SlackSession> sessions) {
        return new SlackRadar(
            farm, props, sessions,
            new ReSafe(
                new ReLogged<>(
                    new ReNotMine(
                        new ReIfDirect(
                            new Reaction.Chain<>(
                                Arrays.asList(
                                    new com.zerocracy.radars.slack.ReRegex("hi|hello|hey", new ReHello()),
                                    new ReProject(),
                                    new RePmo(),
                                    new com.zerocracy.radars.slack.ReRegex(".*", new ReSorry())
                                )
                            ),
                            new ReIfAddressed(
                                new Reaction.Chain<>(
                                    Arrays.asList(
                                        new com.zerocracy.radars.slack.ReRegex("hello|hi|hey", new ReHello()),
                                        new ReProfile(),
                                        new com.zerocracy.radars.slack.ReRegex(".*", new ReSorry())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

}
