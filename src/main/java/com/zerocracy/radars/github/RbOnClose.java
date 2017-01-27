/**
 * Copyright (c) 2016 Zerocracy
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

import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.github.Event;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Label;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.json.JsonObject;

/**
 * Open the issue if it was closed not by the person who opened it.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbOnClose implements Rebound {

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        final String author = issue.author()
            .login().toLowerCase(Locale.ENGLISH);
        final String closer = RbOnClose.closer(issue);
        final String answer;
        if (author.equals(closer)) {
            final Project project = new GhProject(farm, issue.repo());
            try (final Claims claims = new Claims(project).lock()) {
                claims.add(
                    new ClaimOut()
                        .type("pm.scope.wbs.out")
                        .token(new TokenOfIssue(issue))
                        .param("job", new Job(issue))
                );
            }
            answer = String.format(
                "Issue #%d closed by @%s, asked WBS to take it out of scope",
                issue.number(), author
            );
        } else if (issue.isPull()) {
            answer = "It's a pull request";
        } else if (RbOnClose.tagged(issue)) {
            answer = "It's invalid";
        } else {
            issue.open();
            issue.comments().post(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "@%s please, ask @%s to close this issue and [read more](http://www.yegor256.com/2014/11/24/principles-of-bug-tracking.html)",
                    closer, author
                )
            );
            answer = String.format("Ticket re-opened, %s notified", closer);
        }
        return answer;
    }

    /**
     * Get closer's login.
     * @param issue The issue
     * @return Login
     * @throws IOException If fails
     */
    @RetryOnFailure(delay = 1, unit = TimeUnit.SECONDS, verbose = false)
    private static String closer(final Issue.Smart issue) throws IOException {
        return new Event.Smart(
            issue.latestEvent(Event.CLOSED)
        ).author().login().toLowerCase(Locale.ENGLISH);
    }

    /**
     * This issue is tagged as invalid.
     * @param issue The issue
     * @return TRUE if it's invalid
     */
    private static boolean tagged(final Issue issue) {
        final Collection<String> tags = Arrays.asList(
            "invalid", "duplicate", "wontfix"
        );
        boolean tagged = false;
        for (final Label tag : issue.labels().iterate()) {
            if (tags.contains(tag.name())) {
                tagged = true;
                break;
            }
        }
        return tagged;
    }

}
