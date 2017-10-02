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
package com.zerocracy.entry;

import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Region;
import com.jcabi.github.Github;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.SmartFarm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.radars.github.GithubRoutine;
import com.zerocracy.radars.github.TkGithub;
import com.zerocracy.radars.slack.SlackRadar;
import com.zerocracy.radars.slack.TkSlack;
import com.zerocracy.radars.telegram.TelegramRadar;
import com.zerocracy.radars.telegram.TmSession;
import com.zerocracy.tk.TkAlias;
import com.zerocracy.tk.TkApp;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.cactoos.map.StickyMap;
import org.takes.facets.fork.FkRegex;
import org.takes.http.Exit;
import org.takes.http.FtCli;

/**
 * Main entry point.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
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
    @Loggable
    public static void main(final String... args) throws IOException {
        new Main(args).exec();
    }

    /**
     * Run it.
     * @throws IOException If fails on I/O
     */
    @SuppressWarnings("unchecked")
    public void exec() throws IOException {
        final Properties props = new ExtProperties().value();
        if (props.containsKey("testing")) {
            throw new IllegalStateException(
                "Hey, we are in the testing mode!"
            );
        }
        Sentry.init(props.getProperty("sentry.dsn", ""));
        final Map<String, SlackSession> slack = new ExtSlack().value();
        final Github github = new ExtGithub().value();
        final Region dynamo = new ExtDynamo().value();
        final Map<Long, TmSession> tms = new ConcurrentHashMap<>(0);
        final Farm farm = new SmartFarm(
            new S3Farm(new ExtBucket().value()),
            props,
            new StickyMap<>(
                new AbstractMap.SimpleEntry<>("properties", props),
                new AbstractMap.SimpleEntry<>("slack", slack),
                new AbstractMap.SimpleEntry<>("telegram", tms),
                new AbstractMap.SimpleEntry<>("github", github)
            )
        ).value();
        try (
            final SlackRadar radar = new SlackRadar(farm, slack);
            final TelegramRadar telegram = new TelegramRadar(farm, tms)
        ) {
            radar.refresh();
            telegram.start(
                props.getProperty("telegram.token"),
                props.getProperty("telegram.username")
            );
            new GithubRoutine(github).start();
            new FtCli(
                new TkApp(
                    props,
                    farm,
                    new FkRegex("/slack", new TkSlack(farm, props, radar)),
                    new FkRegex("/alias", new TkAlias(farm)),
                    new FkRegex(
                        "/ghook",
                        new TkGithub(farm, github, dynamo, props)
                    )
                ),
                this.arguments
            ).start(Exit.NEVER);
        }
    }

}
