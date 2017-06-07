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

import com.jcabi.aspects.Async;
import com.jcabi.log.Logger;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackChannelJoined;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pmo.Bots;
import com.zerocracy.ext.ExtProperties;
import com.zerocracy.ext.ExtSlack;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

/**
 * Slack listening radar.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RrSlack implements AutoCloseable {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Reaction on post.
     */
    private final Reaction<SlackMessagePosted> posted;

    /**
     * Reaction on invite_friend.
     */
    private final Reaction<SlackChannelJoined> joined;

    /**
     * Slack sessions (Bot ID vs. Session).
     */
    private final Map<String, SlackSession> sessions;

    /**
     * Ctor.
     * @param frm Farm
     * @throws IOException If fails
     */
    public RrSlack(final Farm frm) throws IOException {
        this(frm, new ExtProperties().asValue());
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param props Properties
     */
    public RrSlack(final Farm frm, final Properties props) {
        this(
            frm,
            new ExtSlack().asValue(),
            new ReMailed(
                props,
                new ReSafe(
                    new ReLogged<>(
                        new ReNotMine(
                            new ReIfDirect(
                                new ReProfile(),
                                new ReIfAddressed(
                                    new ReProject()
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param map Map of sessions
     * @param ptd Reaction on post
     */
    RrSlack(final Farm frm, final Map<String, SlackSession> map,
        final Reaction<SlackMessagePosted> ptd) {
        this.farm = frm;
        this.posted = ptd;
        this.joined = new ReLogged<>(
            new ReInvite()
        );
        this.sessions = map;
    }

    /**
     * Refresh all connections for all bots.
     * @throws IOException If fails
     */
    @Async
    public void refresh() throws IOException {
        final Bots bots = new Bots(this.farm).bootstrap();
        final Collection<String> tokens = new HashSet<>(0);
        for (final Map.Entry<String, String> bot : bots.tokens()) {
            tokens.add(bot.getKey());
            if (this.sessions.containsKey(bot.getKey())) {
                continue;
            }
            this.sessions.put(bot.getKey(), this.start(bot.getValue()));
        }
        for (final String bid : this.sessions.keySet()) {
            if (!tokens.contains(bid)) {
                this.sessions.remove(bid);
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (final SlackSession session : this.sessions.values()) {
            session.disconnect();
        }
    }

    /**
     * Create a session.
     * @param token Token
     * @return The session
     * @throws IOException If fails
     */
    private SlackSession start(final String token) throws IOException {
        final SlackSession ssn =
            SlackSessionFactory.createWebSocketSlackSession(token);
        ssn.connect();
        Logger.info(
            this, "Slack connected as @%s/%s to %s",
            ssn.sessionPersona().getUserName(),
            ssn.sessionPersona().getId(),
            ssn.getTeam().getName()
        );
        ssn.addMessagePostedListener(
            (event, sess) -> {
                try {
                    this.posted.react(this.farm, event, ssn);
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        );
        ssn.addChannelJoinedListener(
            (event, sess) -> {
                try {
                    this.joined.react(this.farm, event, ssn);
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        );
        return ssn;
    }

}
