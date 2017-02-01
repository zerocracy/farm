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

import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import java.io.IOException;
import java.util.Collections;
import org.xembly.Directive;

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
    public Iterable<Directive> process(final Project project, final XML xml)
        throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final String[] parts = claim.token().split(";");
        final Repo repo = this.github.repos().get(
            new Coordinates.Simple(parts[1])
        );
        final Issue issue = repo.issues().get(
            Integer.parseInt(parts[2])
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
        return Collections.emptyList();
    }

}
