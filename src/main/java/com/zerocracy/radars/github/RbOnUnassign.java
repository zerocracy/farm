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
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pm.in.Orders;
import java.io.IOException;
import java.util.Locale;
import javax.json.JsonObject;
import org.cactoos.text.FormattedText;

/**
 * Cancel order on unassignment of issue.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class RbOnUnassign implements Rebound {

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        final String sender = event.getJsonObject("sender")
            .getString("login").toLowerCase(Locale.ENGLISH);
        final String job = new Job(issue).toString();
        final Project project = new GhProject(farm, issue.repo());
        final Orders orders = new Orders(farm, project).bootstrap();
        if (orders.assigned(job)) {
            new ClaimOut()
                .type("Notify")
                .token(new TokenOfIssue(issue))
                .param(
                    "message",
                    new Par(
                        "@%s I see that you unassigned this issue;",
                        "the order is still assigned to @%s though;",
                        "to cancel the order use `refuse`, as in §6"
                    ).say(sender, orders.performer(job))
                )
                .postTo(new ClaimsOf(farm, project));
        }
        return new FormattedText(
            "Issue #%d was unassigned", issue.number()
        ).asString();
    }
}
