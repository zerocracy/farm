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
package com.zerocracy.entry;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimsRoutine;
import com.zerocracy.claims.proc.AsyncProc;
import com.zerocracy.claims.proc.BrigadeProc;
import com.zerocracy.claims.proc.CountingProc;
import com.zerocracy.claims.proc.ExpiryProc;
import com.zerocracy.claims.proc.FootprintProc;
import com.zerocracy.claims.proc.MessageMonitorProc;
import com.zerocracy.claims.proc.SentryProc;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.SmartFarm;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
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
        final Props props = new Props();
        if (props.has("//testing")) {
            throw new IllegalStateException(
                "Hey, we are in the testing mode!"
            );
        }
        final long start = System.currentTimeMillis();
        try {
            new Main(args).exec();
        } catch (final Throwable ex) {
            new SafeSentry(new PropsFarm()).capture(ex);
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
        final Path temp = Paths.get("./s3farm").normalize();
        if (!temp.toFile().mkdir()) {
            throw new IllegalStateException(
                String.format(
                    "Failed to mkdir \"%s\"", temp
                )
            );
        }
        Logger.info(this, "Farm is ready to start");
        final ShutdownFarm.Hook shutdown = new ShutdownFarm.Hook();
        final AtomicInteger count = new AtomicInteger();
        final int threads = Runtime.getRuntime().availableProcessors();
        try (
            final Farm farm = new ShutdownFarm(
                new SmartFarm(
                    new S3Farm(new ExtBucket().value(), temp)
                ),
                shutdown
            );
            final SlackRadar radar = new SlackRadar(farm);
            final ClaimsRoutine claims = new ClaimsRoutine(
                farm,
                new AsyncProc(
                    threads,
                    new MessageMonitorProc(
                        farm,
                        new ExpiryProc(
                            new SentryProc(
                                farm,
                                new FootprintProc(
                                    farm,
                                    new CountingProc(
                                        new BrigadeProc(farm),
                                        count
                                    )
                                )
                            )
                        ),
                        shutdown
                    ),
                    shutdown
                ),
                () -> count.intValue() < threads
            )
        ) {
            new ExtMongobee(farm).apply();
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
                    )
                ),
                this.arguments
            ).start(Exit.NEVER);
        }
    }

}
