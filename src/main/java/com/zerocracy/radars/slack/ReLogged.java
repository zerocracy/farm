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
package com.zerocracy.radars.slack;

import com.jcabi.log.Logger;
import com.ullink.slack.simpleslackapi.events.SlackEvent;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import java.io.IOException;

/**
 * Pass through and log.
 *
 * @param <T> Type of event
 * @since 1.0
 */
public final class ReLogged<T extends SlackEvent> implements Reaction<T> {

    /**
     * Reaction.
     */
    private final Reaction<T> origin;

    /**
     * Ctor.
     * @param tgt Target
     */
    public ReLogged(final Reaction<T> tgt) {
        this.origin = tgt;
    }

    @Override
    public boolean react(final Farm farm, final T event,
        final SkSession session) throws IOException {
        if (event instanceof SlackMessagePosted) {
            final SlackMessagePosted posted =
                SlackMessagePosted.class.cast(event);
            Logger.info(
                this,
                // @checkstyle LineLength (1 line)
                "Slack (channel=%s/%s/%s/%s, sub-type=%s, sender=@%s/%s): \"%s\"",
                session.getTeam().getName(),
                posted.getChannel().getId(),
                posted.getChannel().getName(),
                Boolean.toString(posted.getChannel().isDirect()).charAt(0),
                posted.getMessageSubType(),
                posted.getSender().getUserName(),
                posted.getSender().getId(),
                posted.getMessageContent().replaceAll("\\s", " ")
            );
        } else {
            Logger.info(
                this,
                "Slack %s event: %s",
                event.getClass().getName(),
                event.toString()
            );
        }
        return this.origin.react(farm, event, session);
    }

}
