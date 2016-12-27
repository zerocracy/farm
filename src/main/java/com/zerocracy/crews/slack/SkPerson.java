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
import com.zerocracy.pm.Person;

/**
 * Person in slack.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class SkPerson implements Person {

    /**
     * Event.
     */
    private final SlackMessagePosted event;

    /**
     * Session.
     */
    private final SlackSession session;

    /**
     * Ctor.
     * @param evt Event
     * @param ssn Session
     */
    public SkPerson(final SlackMessagePosted evt, final SlackSession ssn) {
        this.event = evt;
        this.session = ssn;
    }

    @Override
    public String name() {
        return String.format(
            "slack:%s",
            this.event.getSender().getId()
        );
    }

    @Override
    public void say(final String message) {
        this.session.sendMessage(
            this.event.getChannel(),
            String.format(
                "> %s%n@%s %s",
                this.event.getMessageContent(),
                this.event.getSender().getUserName(),
                message
            )
        );
    }
}
