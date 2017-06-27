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
package com.zerocracy.radars.slack;

import com.jcabi.aspects.Tv;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.SoftException;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.ThrowableAsBytes;

/**
 * Safe reaction.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class ReSafe implements Reaction<SlackMessagePosted> {

    /**
     * Reaction.
     */
    private final Reaction<SlackMessagePosted> origin;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReSafe(final Reaction<SlackMessagePosted> tgt) {
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        return new IoCheckedFunc<>(
            new FuncWithFallback<Boolean, Boolean>(
                smart -> {
                    boolean result = false;
                    try {
                        result = this.origin.react(farm, event, session);
                    } catch (final SoftException ex) {
                        session.sendMessage(
                            event.getChannel(), ex.getMessage()
                        );
                    }
                    return result;
                },
                throwable -> {
                    session.sendMessage(
                        event.getChannel(),
                        String.join(
                            "",
                            "There is an unrecoverable failure on my side.",
                            " Please, submit it",
                            " [here](https://github.com/zerocracy/datum):",
                            "\n\n```\n",
                            StringUtils.abbreviate(
                                new BytesAsText(
                                    new ThrowableAsBytes(throwable)
                                ).asString(),
                                Tv.THOUSAND
                            ),
                            "\n```"
                        )
                    );
                    throw new IOException(throwable);
                }
            )
        ).apply(true);
    }

}
