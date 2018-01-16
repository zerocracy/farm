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

import com.jcabi.github.Comment;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Response if not my comment.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class ReIfAddressed implements Response {

    /**
     * Response.
     */
    private final Response origin;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReIfAddressed(final Response tgt) {
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        final String self = String.format(
            "@%s", comment.issue().repo().github().users().self().login()
        );
        boolean done = false;
        if (comment.body().contains(self)) {
            final Pattern pattern = Pattern.compile(
                String.format(
                    "\\s*@%s\\s*.+",
                    comment.issue().repo().github().users().self().login()
                ),
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
            );
            if (!pattern.matcher(comment.body()).matches()) {
                throw new SoftException(
                    new Par(
                        "Are you speaking to me or about me?",
                        "You must always start your message with my name",
                        "if you want to address it to me, see §1."
                    ).say()
                );
            }
            done = this.origin.react(farm, comment);
        }
        return done;
    }

}
