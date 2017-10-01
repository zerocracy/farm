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

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.SoftException;
import java.io.IOException;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Proc;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.io.BytesOf;
import org.cactoos.text.TextOf;

/**
 * Rebound that is safe.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
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
                        RbSafe.issue(github, event).comments().post(
                            ex.getLocalizedMessage()
                        );
                        result = ex.getLocalizedMessage();
                    }
                    return result;
                },
                (Proc<Throwable>) throwable -> {
                    RbSafe.issue(github, event).comments().post(
                        String.join(
                            "",
                            "There is an unrecoverable failure on my side.",
                            " Please, submit it",
                            " [here](https://github.com/zerocracy/datum):",
                            "\n\n```\n",
                            StringUtils.abbreviate(
                                new TextOf(
                                    new BytesOf(throwable)
                                ).asString(),
                                // @checkstyle MagicNumber (1 line)
                                1000
                            ),
                            "\n```"
                        )
                    );
                    throw new IOException(throwable);
                }
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
