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
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.zerocracy.crews.github.GhProject;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pm.hr.Roles;
import com.zerocracy.stk.SoftException;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import javax.json.JsonObject;

/**
 * Ping architect on new issues.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
final class RePingArchitect implements Reaction {

    @Override
    public void react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Repo repo = github.repos().get(
            new Coordinates.Simple(
                event.getJsonObject("repository").getString("full_name")
            )
        );
        final Issue issue = repo.issues().get(
            event.getJsonObject("issue").getInt("number")
        );
        final Roles roles = new Roles(new GhProject(farm, repo));
        final String author = new Issue.Smart(issue).author()
            .login().toLowerCase(Locale.ENGLISH);
        try {
            roles.bootstrap();
            final Collection<String> arcs = roles.findByRole("ARC");
            if (!arcs.isEmpty() && !arcs.contains(author)) {
                issue.comments().post(
                    String.format(
                        "@%s please, pay attention to this issue",
                        String.join(", @", arcs)
                    )
                );
            }
        } catch (final SoftException ex) {
            issue.comments().post(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "@%s I'm not managing this repository, please remove the [webhook](https://github.com/%s/settings/hooks) or contact me in [Slack](http://www.zerocracy.com) //cc @yegor256",
                    author, repo.coordinates()
                )
            );
        }
    }
}
