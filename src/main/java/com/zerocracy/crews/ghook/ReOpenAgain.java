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
package com.zerocracy.crews.ghook;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Event;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Locale;
import javax.json.JsonObject;

/**
 * Open the issue if it was closed not by the person who opened it.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
final class ReOpenAgain implements Reaction {

    @Override
    public void react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Repo repo = github.repos().get(
            new Coordinates.Simple(
                event.getJsonObject("repository").getString("full_name")
            )
        );
        final Issue.Smart issue = new Issue.Smart(
            repo.issues().get(
                event.getJsonObject("issue").getInt("number")
            )
        );
        final String author = issue.author()
            .login().toLowerCase(Locale.ENGLISH);
        final String closer = new Event.Smart(
            issue.latestEvent(Event.CLOSED)
        ).author().login().toLowerCase(Locale.ENGLISH);
        if (!author.equals(closer) && !issue.isPull()) {
            issue.open();
            issue.comments().post(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "@%s please, ask @%s to close this issue and [read more](http://www.yegor256.com/2014/11/24/principles-of-bug-tracking.html)",
                    closer, author
                )
            );
        }
    }

}
