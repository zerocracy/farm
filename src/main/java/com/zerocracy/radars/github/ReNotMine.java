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
import java.io.IOException;
import java.util.Locale;

/**
 * Response if not my comment.
 *
 * @since 1.0
 */
public final class ReNotMine implements Response {

    /**
     * Response.
     */
    private final Response origin;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReNotMine(final Response tgt) {
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        final String author = comment.author()
            .login().toLowerCase(Locale.ENGLISH);
        final String self = comment.issue().repo().github()
            .users().self().login().toLowerCase(Locale.ENGLISH);
        boolean done = false;
        if (!author.equals(self)) {
            done = this.origin.react(farm, comment);
        }
        return done;
    }

}
