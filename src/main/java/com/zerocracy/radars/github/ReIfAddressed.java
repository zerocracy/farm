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
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Response if not my comment.
 *
 * @since 1.0
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
        final String login = comment.issue().repo().github()
            .users().self().login().toLowerCase(Locale.ENGLISH);
        final Pattern mentioned = Pattern.compile(
            String.format("@%s(?![a-zA-Z0-9-])", Pattern.quote(login))
        );
        final String body = comment.body().trim();
        boolean done = false;
        if (mentioned.matcher(body).find()) {
            if (!body.startsWith(String.format("@%s ", login))) {
                throw new SoftException(
                    new Par(
                        "Are you speaking to me or about me",
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "[here](https://github.com/%s/issues/%d#issuecomment-%d);",
                            comment.issue().repo().coordinates(),
                            comment.issue().number(), comment.number()
                        ),
                        "you must always start your message with my name",
                        "if you want to address it to me, see §1"
                    ).say()
                );
            }
            done = this.origin.react(farm, comment);
        }
        return done;
    }

}
