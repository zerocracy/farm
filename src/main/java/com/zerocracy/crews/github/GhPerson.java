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
package com.zerocracy.crews.github;

import com.jcabi.github.Comment;
import com.zerocracy.crews.SoftException;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.Person;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.util.Iterator;

/**
 * Person in GitHub.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class GhPerson implements Person {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Comment.
     */
    private final Comment comment;

    /**
     * Ctor.
     * @param pkt Project
     * @param cmt Comment
     */
    GhPerson(final Project pkt, final Comment cmt) {
        this.project = pkt;
        this.comment = cmt;
    }

    @Override
    public String name() throws IOException {
        final Iterator<String> list = new People(this.project).find(
            "github", new Comment.Smart(this.comment).author().login()
        ).iterator();
        if (!list.hasNext()) {
            throw new SoftException(
                String.join(
                    " ",
                    "I don't know who you are, please contact me in Slack",
                    "and introduce yourself."
                )
            );
        }
        return list.next();
    }

    @Override
    public void say(final String message) throws IOException {
        this.comment.issue().comments().post(
            String.format(
                "> %s%n%n%s",
                new Comment.Smart(this.comment).body(),
                message
            )
        );
    }

}
