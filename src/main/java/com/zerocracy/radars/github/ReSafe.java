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
import com.zerocracy.SoftException;
import com.zerocracy.farm.props.Props;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tools.TxtUnrecoverableError;
import java.io.IOException;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;

/**
 * Safe Reaction on GitHub comment.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (2 lines)
 */
public final class ReSafe implements Response {

    /**
     * Response.
     */
    private final Response origin;

    /**
     * Ctor.
     * @param rsp Response
     */
    public ReSafe(final Response rsp) {
        this.origin = rsp;
    }

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        return new IoCheckedFunc<>(
            new FuncWithFallback<Boolean, Boolean>(
                smart -> {
                    boolean result = false;
                    try {
                        result = this.origin.react(farm, comment);
                    } catch (final SoftException ex) {
                        comment.issue().comments().post(
                            ex.getLocalizedMessage()
                        );
                    }
                    return result;
                },
                new FuncOf<>(
                    throwable -> {
                        new ThrottledComments(comment.issue().comments()).post(
                            new TxtUnrecoverableError(
                                throwable, new Props(farm),
                                String.format(
                                    "Issue: %s#%d, Comment: %d",
                                    comment.issue().repo().coordinates(),
                                    comment.issue().number(),
                                    comment.number()
                                )
                            ).asString()
                        );
                        new SafeSentry(farm).capture(throwable);
                        throw new IOException(throwable);
                    }
                )
            )
        ).apply(true);
    }
}
