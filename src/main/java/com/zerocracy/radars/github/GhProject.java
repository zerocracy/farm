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

import com.jcabi.github.Comment;
import com.jcabi.github.Repo;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Iterator;

/**
 * Surrogate project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class GhProject implements Project {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Repo.
     */
    private final Repo repo;

    /**
     * Ctor.
     * @param frm Farm
     * @param cmt Comment
     */
    public GhProject(final Farm frm, final Comment cmt) {
        this(frm, cmt.issue().repo());
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param rpo Repo
     */
    public GhProject(final Farm frm, final Repo rpo) {
        this.farm = frm;
        this.repo = rpo;
    }

    @Override
    public String toString() {
        try {
            return this.project().toString();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Item acq(final String file) throws IOException {
        return this.project().acq(file);
    }

    /**
     * Make it.
     * @return Project
     * @throws IOException If I/O files
     */
    private Project project() throws IOException {
        final Iterator<Project> list = this.farm.find(
            String.format(
                "links/link[@rel='github' and @href='%s']",
                this.repo.coordinates().toString()
            )
        ).iterator();
        if (!list.hasNext()) {
            throw new SoftException(
                String.join(
                    " ",
                    "I'm not managing this GitHub repository.",
                    "You have to contact me in Slack first."
                )
            );
        }
        return list.next();
    }

}
