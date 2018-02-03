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
package com.zerocracy.radars.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import com.zerocracy.err.FbReaction;
import com.zerocracy.err.FbSend;
import java.io.IOException;

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
     * Reaction with fallback.
     */
    private final FbReaction fbr;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReSafe(final Reaction<SlackMessagePosted> tgt) {
        this.origin = tgt;
        this.fbr = new FbReaction();
    }

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        return this.fbr.react(
            () -> this.origin.react(farm, event, session),
            new FbSend(
                msg -> session.sendMessage(event.getChannel(), msg),
                farm
            )
        );
    }

}
