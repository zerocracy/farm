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
package com.zerocracy.crews.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * React by regex.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class ReRegex implements Reaction<SlackMessagePosted> {

    /**
     * Pattern.
     */
    private final Pattern pattern;

    /**
     * Reply.
     */
    private final Reply origin;

    /**
     * Ctor.
     * @param ptn Pattern
     * @param tgt Reply
     */
    ReRegex(final String ptn, final Reply tgt) {
        this(Pattern.compile(ptn), tgt);
    }

    /**
     * Ctor.
     * @param ptn Pattern
     * @param tgt Reply
     */
    ReRegex(final Pattern ptn, final Reply tgt) {
        this.pattern = ptn;
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        final String msg = event.getMessageContent().split(" ", 2)[1];
        final Matcher matcher = this.pattern.matcher(msg);
        boolean done = false;
        if (matcher.matches()) {
            this.origin.react(
                farm, event,
                message -> session.sendMessage(
                    event.getChannel(),
                    String.format(
                        "> %s\n@%s %s",
                        event.getMessageContent(),
                        event.getSender().getUserName(),
                        message
                    )
                )
            );
            done = true;
        }
        return done;
    }

}
