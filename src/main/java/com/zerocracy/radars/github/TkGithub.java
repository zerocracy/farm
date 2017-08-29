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
package com.zerocracy.radars.github;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.dynamo.Region;
import com.jcabi.github.Github;
import com.zerocracy.jstk.Farm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.json.Json;
import javax.json.stream.JsonParsingException;
import org.cactoos.func.UncheckedProc;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.form.RqFormBase;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsText;
import org.takes.rs.RsWithStatus;

/**
 * GitHub hook, take.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@ScheduleWithFixedDelay
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkGithub implements Take, Runnable {

    /**
     * Reaction.
     */
    private final Rebound rebound;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Github.
     */
    private final Github github;

    /**
     * Ctor.
     * @param frm Farm
     * @param ghub Github
     * @param dynamo DynamoDB
     * @param props Properties
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public TkGithub(
        final Farm frm,
        final Github ghub,
        final Region dynamo,
        final Properties props
    ) {
        this(
            frm,
            ghub,
            new RbLogged(
                new RbSafe(
                    new RbByActions(
                        new RbOnComment(
                            new GithubFetch(
                                frm,
                                ghub,
                                new ReLogged(
                                    new Reaction.Chain(
                                        new ReOnReason(
                                            "mention",
                                            new ReOnComment(
                                                ghub,
                                                new ReSafe(
                                                    new ReNotMine(
                                                        new ReIfAddressed(
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
                        new RbOnPullRequest(),
                        "opened", "reopened"
                    ),
                    new RbByActions(
                        new RbPingArchitect(),
                        "opened", "reopened"
                    ),
                    new RbByActions(
                        new Rebound.Chain(
                            new RbVerifyCloser(),
                            new RbOnClose()
                        ),
                        "closed"
                    ),
                    new RbByActions(
                        new RbByLabel(
                            new RbOnBug(),
                            "bug"
                        ),
                        "labeled"
                    ),
                    new RbByActions(new RbOnAssign(), "assigned"),
                    new RbByActions(new RbOnUnassign(), "unassigned"),
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
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param ghub Github
     * @param rbd Rebound
     */
    public TkGithub(final Farm frm, final Github ghub, final Rebound rbd) {
        this.farm = frm;
        this.github = ghub;
        this.rebound = rbd;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String body = new RqFormSmart(
            new RqFormBase(req)
        ).single("payload");
        try {
            return new RsWithStatus(
                new RsText(
                    this.rebound.react(
                        this.farm,
                        this.github,
                        Json.createReader(
                            new ByteArrayInputStream(
                                body.getBytes(StandardCharsets.UTF_8)
                            )
                        ).readObject()
                    )
                ),
                HttpURLConnection.HTTP_OK
            );
        } catch (final JsonParsingException ex) {
            throw new IllegalArgumentException(
                String.format("Can't parse JSON: %s", body),
                ex
            );
        }
    }

    @Override
    public void run() {
        new UncheckedProc<>(
            new AcceptInvitations(this.github)
        ).exec(true);
    }
}
