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
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import java.io.IOException;
import java.util.Locale;
import javax.json.JsonObject;
import org.cactoos.text.FormattedText;

/**
 * Request order start on assignment of issue.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class RbOnAssign implements Rebound {

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        final FormattedText reply;
        final String login = new GhIssueEvent(event).assignee();
        final String sender = event.getJsonObject("sender")
            .getString("login").toLowerCase(Locale.ENGLISH);
        if ("0crat".equalsIgnoreCase(login)) {
            issue.assign("");
            new ClaimOut()
                .type("Add job to WBS")
                .token(new TokenOfIssue(issue))
                .param("job", new Job(issue))
                // @checkstyle AvoidInlineConditionalsCheck (1 line)
                .param("role", issue.isPull() ? "REV" : "DEV")
                .param("reason", "GitHub issue was assigned to 0crat")
                .postTo(new ClaimsOf(farm, new GhProject(farm, issue.repo())));
            reply = new FormattedText(
                "Issue #%d assigned to 0crat, adding to WBS",
                issue.number()
            );
        } else if ("0crat".equalsIgnoreCase(sender)) {
            reply = new FormattedText(
                "Issue #%d was assigned by 0crat, we ignore this situation",
                issue.number()
            );
        } else {
            new ClaimOut()
                .type("Request order start")
                .token(new TokenOfIssue(issue))
                .author(sender)
                .param("login", login)
                .param("job", new Job(issue))
                .param(
                    "reason",
                    String.format("GitHub issue was assigned by @%s", sender)
                )
                .postTo(new ClaimsOf(farm, new GhProject(farm, issue.repo())));
            reply = new FormattedText(
                "Issue #%d assigned to %s via Github",
                issue.number(), login
            );
        }
        return reply.asString();
    }
}
