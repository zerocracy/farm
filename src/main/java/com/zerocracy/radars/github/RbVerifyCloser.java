/**
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

import com.jcabi.github.Event;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Locale;
import javax.json.JsonObject;

/**
 * Issue close-event reaction. Check that issue closer is an author of it.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16.1
 * @todo #156:30m We need to check issue closer's role.
 *  If his role is 'PO' or 'ARC' we should skip warning message
 *  about bad practice and just say that
 *  issue has been closed by ARC (or PO).
 */
final class RbVerifyCloser implements Rebound {

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Issue.Smart issue = new Issue.Smart(
            new IssueOfEvent(github, event)
        );
        final String answer;
        final String closer = RbVerifyCloser.closer(issue);
        final String author = issue.author()
            .login().toLowerCase(Locale.ENGLISH);
        if (issue.isPull()) {
            answer = String.format(
                "Pull request #%d closed by @%s",
                issue.number(), closer
            );
        } else {
            if (author.equals(closer)) {
                answer = String.format(
                    "Issue #%d closed by @%s",
                    issue.number(), closer
                );
            } else {
                answer = String.format(
                    String.join(
                        " ",
                        "%s, it's a bad practice to close tickets",
                        "which were opened by someone else (%s)!"
                    ),
                    closer, author
                );
            }
        }
        return answer;
    }

    /**
     * Get closer's login.
     * @param issue The issue
     * @return Login
     * @throws IOException If fails
     */
    private static String closer(final Issue.Smart issue) throws IOException {
        return new Event.Smart(
            issue.latestEvent(Event.CLOSED)
        ).author().login().toLowerCase(Locale.ENGLISH);
    }
}
