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

import com.zerocracy.entry.ExtDynamo;
import com.zerocracy.entry.ExtGithub;
import com.zerocracy.farm.props.Props;
import com.zerocracy.jstk.Farm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.stream.JsonParsingException;
import org.cactoos.func.UncheckedProc;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqForm;
import org.takes.rq.form.RqFormBase;
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
     * @throws IOException If fails
     */
    public TkGithub(final Farm frm) throws IOException {
        this(frm, new Props(frm));
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param props Props
     * @throws IOException If fails
     */
    public TkGithub(final Farm frm, final Props props) throws IOException {
        this(
            frm,
            new RbDelayed(
                new RbLogged(
                    new RbSafe(
                        new RbByActions(
                            new RbOnComment(
                                new ReLogged(
                                    new Reaction.Chain(
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
                                                new ExtDynamo(frm).value()
                                                    .table("0crat-github")
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
                            new ExtDynamo(frm).value().table("0crat-tweets"),
                            props.get("//twitter/key"),
                            props.get("//twitter/secret"),
                            props.get("//twitter/token"),
                            props.get("//twitter/tsecret")
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

    @Override
    public Response act(final Request req) throws IOException {
        final RqForm form = new RqFormBase(req);
        final Iterable<String> body = form.param("payload");
        if (!body.iterator().hasNext()) {
            throw new RsForward(
                new RsFlash(
                    // @checkstyle LineLength (1 line)
                    "We expect this URL to be called by GitHub with JSON as 'payload' form parameter."
                )
            );
        }
        try {
            return new RsWithStatus(
                new RsText(
                    this.rebound.react(
                        this.farm,
                        new ExtGithub(this.farm).value(),
                        Json.createReader(
                            new ByteArrayInputStream(
                                body.iterator().next().getBytes(
                                    StandardCharsets.UTF_8
                                )
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
            new AcceptInvitations(new ExtGithub(this.farm).value())
        ).exec(true);
    }
}
