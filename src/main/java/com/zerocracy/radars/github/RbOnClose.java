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

import com.jcabi.aspects.Tv;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Label;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import javax.json.JsonObject;

/**
 * Issue close-event reaction. Remove it from WBS.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbOnClose implements Rebound {

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        final String answer;
        final String job = new Job(issue).toString();
        final Project project = new GhProject(farm, issue.repo());
        final String author = event.getJsonObject("sender")
            .getString("login")
            .toLowerCase(Locale.ENGLISH);
        if (RbOnClose.tagged(issue)) {
            new ClaimOut()
                .type("Cancel order")
                .token(new TokenOfIssue(issue))
                .author(author)
                .param("job", job)
                .param(
                    "reason",
                    new Par(
                        "GitHub issue was closed as 'invalid' by @%s"
                    ).say(author)
                )
                .postTo(new ClaimsOf(farm, project));
            new ClaimOut()
                .type("Remove job from WBS")
                .token(new TokenOfIssue(issue))
                .author(author)
                .param("job", job)
                .postTo(new ClaimsOf(farm, project));
            answer = "It's invalid";
        } else {
            new ClaimOut()
                .type("Close job")
                .token(new TokenOfIssue(issue))
                .author(author)
                .param("job", job)
                .until(Duration.ofMinutes((long) Tv.FIFTEEN))
                .param("reason", "GitHub issue was closed")
                .postTo(new ClaimsOf(farm, project));
            answer = "Asked WBS to take it out of scope";
        }
        return answer;
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
