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

import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/**
 * Tube to GitHub.
 *
 * @since 1.0
 */
public final class GhTube {

    /**
     * Original Github comment.
     */
    private final Comment comment;

    /**
     * Ctor.
     * @param cmt Comment
     */
    public GhTube(final Comment cmt) {
        this.comment = cmt;
    }

    /**
     * Say it.
     * @param msg Message
     * @throws IOException If fails
     */
    public void say(final String msg) throws IOException {
        new ThrottledComments(this.comment.issue().comments()).post(
            String.format(
                // @checkstyle LineLength (1 line)
                "> %s ([here](https://github.com/%s/issues/%d#issuecomment-%d))%n%n@%s %s",
                StringUtils.abbreviate(
                    new Comment.Smart(this.comment).body(),
                    // @checkstyle MagicNumber (1 line)
                    100
                ).replaceAll("\\s+", " "),
                this.comment.issue().repo().coordinates(),
                this.comment.issue().number(),
                this.comment.number(),
                new Comment.Smart(this.comment).author().login(),
                msg
            )
        );
        Logger.info(
            this, "GitHub comment at %s#%d",
            this.comment.issue().repo().coordinates(),
            this.comment.issue().number()
        );
    }

}
