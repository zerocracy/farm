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
import com.jcabi.github.Issue;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.SoftException;
import com.zerocracy.entry.ExtDynamo;
import com.zerocracy.farm.Errors;
import com.zerocracy.farm.props.Props;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tools.TxtUnrecoverableError;
import java.io.IOException;
import javax.json.JsonObject;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;

/**
 * Rebound that is safe.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbSafe implements Rebound {

    /**
     * Original reaction.
     */
    private final Rebound origin;

    /**
     * Ctor.
     * @param chain Reactions
     */
    public RbSafe(final Rebound... chain) {
        this.origin = new Rebound.Chain(chain);
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        return new IoCheckedFunc<>(
            new FuncWithFallback<Boolean, String>(
                smart -> {
                    String result;
                    try {
                        result = this.origin.react(farm, github, event);
                    } catch (final SoftException ex) {
                        new ThrottledComments(
                            RbSafe.issue(github, event).comments()
                        ).post(ex.getLocalizedMessage());
                        result = ex.getLocalizedMessage();
                    }
                    return result;
                },
                new FuncOf<>(
                    throwable -> {
                        final Issue issue = RbSafe.issue(github, event);
                        Logger.error(
                            this,
                            "Issue: %s#%d, Action: `%s`, error: %[exception]s",
                            issue.repo().coordinates(),
                            issue.number(),
                            event.getString("action"),
                            throwable
                        );
                        new SafeSentry(farm).capture(
                            new IllegalArgumentException(
                                event.toString(), throwable
                            )
                        );
                        new Errors.Github(
                            new Errors(new ExtDynamo(farm).value()),
                            github
                        ).add(
                            new ThrottledComments(issue.comments()).post(
                                new TxtUnrecoverableError(
                                    throwable, new Props(farm),
                                    String.format(
                                        "Issue: %s#%d, Action: `%s`",
                                        issue.repo().coordinates(),
                                        issue.number(),
                                        event.getString("action")
                                    )
                                ).asString()
                            )
                        );
                        throw new IOException(throwable);
                    }
                )
            )
        ).apply(true);
    }

    /**
     * Repository.
     * @param github Github client
     * @param json JSON event
     * @return Repository
     * @throws IOException If fails
     */
    private static Issue issue(final Github github, final JsonObject json)
        throws IOException {
        final Issue issue;
        if (json.containsKey("issue")) {
            issue = new IssueOfEvent(github, json);
        } else {
            issue = new MkGithub().randomRepo().issues().create(
                "No title", "No body"
            );
        }
        return issue;
    }

}
