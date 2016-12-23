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

import com.jcabi.log.Logger;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Slack listening crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SlackCrew implements Crew {

    /**
     * Session token.
     */
    private final String token;

    /**
     * Slack session.
     */
    private final AtomicReference<SlackSession> session;

    /**
     * Ctor.
     * @param tkn Token
     */
    public SlackCrew(final String tkn) {
        this.token = tkn;
        this.session = new AtomicReference<>();
    }

    @Override
    public void deploy(final Farm farm) throws IOException {
        if (this.session.get() == null) {
            this.session.set(this.start(farm));
        }
    }

    /**
     * Create a session.
     * @param farm The farm
     * @return The session
     * @throws IOException If fails
     */
    private SlackSession start(final Farm farm) throws IOException {
        final SlackSession ssn =
            SlackSessionFactory.createWebSocketSlackSession(this.token);
        final Reaction reaction = new ReNotMine(
            ssn.sessionPersona().getUserName(),
            new Reaction.Chain(

            )
        );
        ssn.addMessagePostedListener(
            (event, sess) -> {
                try {
                    reaction.react(farm, event);
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
                final SlackChannel channel = event.getChannel();
                final SlackUser sender = event.getSender();
                sess.sendMessage(channel, "hey");
            }
        );
        ssn.connect();
        Logger.info(
            this, "Slack connected as @%s",
            ssn.sessionPersona().getUserName()
        );
        return ssn;
    }

}
