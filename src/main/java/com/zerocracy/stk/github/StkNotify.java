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
package com.zerocracy.stk.github;

import com.google.common.collect.Lists;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Bulk;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Smarts;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.radars.github.GhTube;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Notify in GitHub.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.8
 */
public final class StkNotify implements Stakeholder {

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Ctor.
     * @param ghb Github client
     */
    public StkNotify(final Github ghb) {
        this.github = ghb;
    }

    @Override
    public void process(final Project project, final XML xml)
        throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final String[] parts = claim.token().split(";");
        final Repo repo = this.github.repos().get(
            new Coordinates.Simple(parts[1])
        );
        final Issue issue = StkNotify.safe(
            repo.issues().get(
                Integer.parseInt(parts[2])
            )
        );
        final String message = claim.param("message");
        if (parts.length > Tv.THREE) {
            final Comment comment = issue.comments().get(
                Integer.parseInt(parts[Tv.THREE])
            );
            new GhTube(comment).say(message);
        } else {
            issue.comments().post(message);
        }
    }

    /**
     * Return issue if it's safe to post there.
     * @param issue Original issue
     * @return Issue that is safe
     * @throws IOException If fails
     */
    private static Issue safe(final Issue issue) throws IOException {
        final List<Comment.Smart> comments = Lists.newArrayList(
            new Bulk<>(
                new Smarts<>(
                    issue.comments().iterate(new Date(0L))
                )
            )
        );
        Collections.reverse(comments);
        if (StkNotify.over(issue, comments)) {
            throw new IllegalStateException(
                String.format(
                    "Can't post anything to %s#%d, too many comments already",
                    issue.repo().coordinates(), issue.number()
                )
            );
        }
        return issue;
    }

    /**
     * TRUE if too many already.
     * @param issue Original issue
     * @param list List of comments
     * @return TRUE if over limit
     * @throws IOException If fails
     */
    private static boolean over(final Issue issue,
        final List<Comment.Smart> list) throws IOException {
        final String self = issue.repo().github().users().self().login();
        boolean over = false;
        for (int idx = 0; idx < list.size(); ++idx) {
            if (idx >= Tv.FIVE) {
                over = true;
                break;
            }
            if (!list.get(idx).author().login().equalsIgnoreCase(self)) {
                break;
            }
        }
        return over;
    }
}
