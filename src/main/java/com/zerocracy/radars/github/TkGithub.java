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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.entry.ExtDynamo;
import com.zerocracy.entry.ExtGithub;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tk.RsParFlash;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import org.cactoos.func.IoCheckedProc;
import org.takes.HttpException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqForm;
import org.takes.rq.form.RqFormBase;
import org.takes.rs.RsText;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithStatus;

/**
 * GitHub hook, take.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
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
     * Ctor.
     * @param frm Farm
     * @checkstyle LineLength (400 lines)
     */
    public TkGithub(final Farm frm) {
        this(
            frm,
            new RbAsync(
                new RbSync(
                    new RbAccessible(
                        new RbDelayed(
                            new RbLogged(
                                new RbSafe(
                                    new RbByActions(
                                        new RbOnComment(
                                            new ReLogged(
                                                new ReOnReason(
                                                    "mention",
                                                    new ReOnComment(
                                                        new ExtGithub(frm).value(),
                                                        new ReSafe(
                                                            new ReNotMine(
                                                                new ReIfAddressed(
                                                                    new ReQuestion()
                                                                )
                                                            )
                                                        ),
                                                        new ExtDynamo(frm).value().table("0crat-github")
                                                    )
                                                )
                                            )
                                        ),
                                        "created"
                                    ),
                                    new RbByActions(
                                        new RbMilestone(),
                                        "created"
                                    ),
                                    new RbByActions(
                                        new RbAddToMilestone(),
                                        "milestoned"
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
                                        new RbOnClose(),
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
                                    new RbByActions(new RbRelease(), "published")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param rbd Rebound
     */
    public TkGithub(final Farm frm, final Rebound rbd) {
        this.farm = frm;
        this.rebound = rbd;
    }

    // @todo #1390:30min Adjust call to Quota.over with proper message and
    //  create test case for the scenario it returns true.
    //  PR https://github.com/zerocracy/farm/pull/1501 introduced this change
    //  on Quota.over and has example of its adjustment and test for
    //  AcceptInvitations.
    @Override
    public Response act(final Request req) throws IOException {
        final RqForm form = new RqFormBase(req);
        final Iterable<String> body;
        try {
            body = form.param("payload");
        } catch (final IllegalArgumentException err) {
            throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, err);
        }
        if (!body.iterator().hasNext()) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "We expect this URL to be called by GitHub",
                        "with JSON as 'payload' form parameter"
                    ).say(),
                    Level.WARNING
                )
            );
        }
        final Github github = new ExtGithub(this.farm).value();
        if (new Quota(github).over()) {
            throw new RsForward(
                new RsWithBody(
                    new Par(
                        "GitHub API is over quota: %s"
                    ).say(new Quota(github))
                ),
                HttpURLConnection.HTTP_UNAVAILABLE
            );
        }
        return new RsWithStatus(
            new RsText(
                this.rebound.react(
                    this.farm,
                    github,
                    TkGithub.json(body.iterator().next())
                )
            ),
            HttpURLConnection.HTTP_OK
        );
    }

    @Override
    public void run() {
        try {
            new IoCheckedProc<>(
                new AcceptInvitations(new ExtGithub(this.farm).value())
            ).exec(true);
        } catch (final IOException err) {
            new SafeSentry(this.farm).capture(err);
        }
    }

    /**
     * Read JSON from body.
     * @param body The body
     * @return The JSON object
     */
    private static JsonObject json(final String body) {
        try (
            final JsonReader reader =
                Json.createReader(
                    new ByteArrayInputStream(
                        body.getBytes(StandardCharsets.UTF_8)
                    )
                )
        ) {
            return reader.readObject();
        } catch (final JsonParsingException ex) {
            throw new IllegalArgumentException(
                String.format("Can't parse JSON: %s", body),
                ex
            );
        }
    }
}
