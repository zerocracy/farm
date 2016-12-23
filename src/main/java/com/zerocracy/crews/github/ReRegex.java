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
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Response if regex matches.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class ReRegex implements Response {

    /**
     * Regex.
     */
    private final Pattern regex;

    /**
     * Reply.
     */
    private final Reply origin;

    /**
     * Ctor.
     * @param ptn Pattern
     * @param tgt Target
     */
    ReRegex(final String ptn, final Reply tgt) {
        this(Pattern.compile(ptn), tgt);
    }

    /**
     * Ctor.
     * @param ptn Pattern
     * @param tgt Target
     */
    ReRegex(final Pattern ptn, final Reply tgt) {
        this.regex = ptn;
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        final Matcher matcher = this.regex.matcher(
            comment.body().split(" ", 2)[1]
        );
        boolean done = false;
        if (matcher.matches()) {
            this.origin.react(
                farm, comment,
                message -> comment.issue().comments().post(
                    String.format(
                        "> %s\n\n%s",
                        comment.body(),
                        message
                    )
                )
            );
            done = true;
        }
        return done;
    }
}
