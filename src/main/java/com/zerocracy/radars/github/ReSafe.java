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
import com.zerocracy.SoftException;
import com.zerocracy.err.FbReaction;
import com.zerocracy.err.ReFallback;
import com.zerocracy.farm.props.Props;
import com.zerocracy.msg.TxtUnrecoverableError;
import java.io.IOException;

/**
 * Safe Reaction on GitHub comment.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class ReSafe implements Response {
    /**
     * Response.
     */
    private final Response origin;
    /**
     * Reaction with fallback.
     */
    private final FbReaction fbr;

    /**
     * Ctor.
     * @param rsp Response
     */
    public ReSafe(final Response rsp) {
        this.origin = rsp;
        this.fbr = new FbReaction();
    }

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        return this.fbr.react(
            () -> this.origin.react(farm, comment),
            new ReFallback() {
                @Override
                public void process(final SoftException err)
                    throws IOException {
                    comment.issue().comments().post(err.getLocalizedMessage());
                }

                @Override
                public void process(final Exception err) throws IOException {
                    new ThrottledComments(comment.issue().comments()).post(
                        new TxtUnrecoverableError(
                            err, new Props(farm)
                        ).asString()
                    );
                }
            }
        );
    }
}
