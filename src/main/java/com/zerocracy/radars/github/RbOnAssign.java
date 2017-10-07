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

import com.jcabi.github.Event;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Awards;
import java.io.IOException;
import java.util.Locale;
import javax.json.JsonObject;
import org.cactoos.text.FormattedText;

/**
 * Request order start on assignment of issue.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @since 0.16.1
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
        final String login = issue.assignee().login();
        final FormattedText reply;
        if ("0crat".equalsIgnoreCase(login)) {
            issue.assign("");
            new ClaimOut()
                .type("Add job to WBS")
                .token(new TokenOfIssue(issue))
                .param("job", new Job(issue))
                .postTo(new GhProject(farm, issue.repo()));
            reply = new FormattedText(
                "Issue #%d assigned to 0crat, adding to WBS",
                issue.number()
            );
        } else {
            new ClaimOut()
                .type("Request order start")
                .token(new TokenOfIssue(issue))
                .author(event.getJsonObject("sender").getString("login"))
                .param("login", login)
                .param("job", new Job(issue))
                .postTo(new GhProject(farm, issue.repo()));
            reply = new FormattedText(
                "Issue #%d assigned to %s via Github",
                issue.number(), login
            );
        }
        final Project project = new GhProject(farm, issue.repo());
        final Job job = new Job(issue);
        new ClaimOut()
            .type("Request order start")
            .token(new TokenOfIssue(issue))
            .author(event.getJsonObject("sender").getString("login"))
            .param("login", login)
            .param("job", job)
            .postTo(new GhProject(farm, issue.repo()));
        final String author = new Event.Smart(
            issue.latestEvent(Event.ASSIGNED)
        ).author().login().toLowerCase(Locale.ENGLISH);
        if (new Roles(project).bootstrap().hasRole(author, "ARC")) {
            final int minutes = -5;
            final Awards awards = new Awards(project, author).bootstrap();
            final String reason =
                "Manual assignment of issues is discouraged, see par.19";
            awards.add(minutes, job.toString(), reason);
            new ClaimOut()
                .type("Award points were added")
                .param("job", job)
                .param("login", author)
                .param("points", minutes)
                .param("reason", reason)
                .postTo(project);
        }
        return reply.asString();
    }
}
