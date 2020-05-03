/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.entry;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.zerocracy.TempFiles;
import com.zerocracy.claims.ClaimGuts;
import com.zerocracy.claims.ClaimsFarm;
import com.zerocracy.claims.ClaimsRoutine;
import com.zerocracy.claims.MessageSink;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.SmartFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.sync.TestLocks;
import com.zerocracy.radars.github.GithubRoutine;
import com.zerocracy.radars.github.TkGithub;
import com.zerocracy.radars.gitlab.TkGitlab;
import com.zerocracy.radars.slack.SlackRadar;
import com.zerocracy.radars.slack.TkSlack;
import com.zerocracy.radars.viber.TkViber;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.shutdown.ShutdownFarm;
import com.zerocracy.tk.TkAlias;
import com.zerocracy.tk.TkApp;
import com.zerocracy.tk.TkSentry;
import com.zerocracy.tk.TkZoldCallback;
import java.io.IOException;
import java.util.Collections;
import javax.ws.rs.HttpMethod;
import org.cactoos.func.AsyncFunc;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkMethods;
import org.takes.http.Exit;
import org.takes.http.FtCli;

/**
 * Main entry point.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
     * @checkstyle IllegalCatchCheck (20 lines)
     */
    @Loggable
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static void main(final String... args) throws IOException {
        final long start = System.currentTimeMillis();
        try {
            new Main(args).exec();
        } catch (final Throwable ex) {
            new SafeSentry(
                new PropsFarm(query -> Collections.emptyList())
            ).capture(ex);
            Logger.error(Main.class, "The main app crashed: %[exception]s", ex);
            throw new IOException(ex);
        } finally {
            Logger.info(
                Main.class, "Finished after %[ms]s of activity",
                System.currentTimeMillis() - start
            );
        }
    }

    /**
     * Run it.
     * @throws IOException If fails on I/O
     */
    @SuppressWarnings("unchecked")
    public void exec() throws IOException {
        Logger.info(this, "Farm is ready to start");
        final ShutdownFarm.Hook shutdown = new ShutdownFarm.Hook();
        final ClaimGuts cgts = new ClaimGuts();
        final TestLocks locks = new TestLocks();
        try (
            final MessageSink farm = new MessageSink(
                new ShutdownFarm(
                    new ClaimsFarm(
                        new TempFiles.Farm(
                            new SmartFarm(
                                new S3Farm(new ExtBucket().value(), locks),
                                locks
                            )
                        ),
                        cgts
                    ),
                    shutdown
                ),
                shutdown
            );
            final SlackRadar radar = new SlackRadar(farm);
            final ClaimsRoutine claims = new ClaimsRoutine(farm)
        ) {
            new ExtMongobee(farm).apply();
            farm.start(claims.messages());
            cgts.add(claims.messages());
            claims.start(shutdown);
            new AsyncFunc<>(
                input -> {
                    new ExtTelegram(farm).value();
                }
            ).exec(null);
            new AsyncFunc<>(
                input -> {
                    radar.refresh();
                }
            ).exec(null);
            new GithubRoutine(farm).start();
            new Pings(farm).start();
            new FtCli(
                new TkApp(
                    farm,
                    new FkRegex("/alias", new TkAlias(farm)),
                    new FkRegex("/slack", new TkSlack(farm, radar)),
                    new FkRegex("/viber", new TkViber(farm)),
                    new FkRegex(
                        "/ghook",
                        new TkMethods(
                            new TkSentry(farm, new TkGithub(farm)),
                            HttpMethod.POST
                        )
                    ),
                    new FkRegex(
                        "/glhook",
                        new TkMethods(
                            new TkSentry(farm, new TkGitlab()),
                            HttpMethod.POST
                        )
                    ),
                    new FkRegex(
                        "/zcallback",
                        new TkZoldCallback(farm)
                    )
                ),
                this.arguments
            ).start(Exit.NEVER);
        }
    }
}
