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
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackChannelJoined;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtSlack;
import com.zerocracy.pmo.Bots;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.cactoos.Func;
import org.cactoos.func.UncheckedFunc;

/**
 * Slack listening radar.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SlackRadar implements AutoCloseable {

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
     * Slack session provider.
     */
    private final UncheckedFunc<String, SkSession> slackssess;

    /**
     * Ctor.
     * @param frm Farm
     */
    public SlackRadar(final Farm frm) {
        this(
            frm,
            new ReMailed(
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
            ),
            SlackSessionFactory::createWebSocketSlackSession
        );
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param ptd Reaction on post
     * @param sess Session generator
     */
    SlackRadar(final Farm frm, final Reaction<SlackMessagePosted> ptd,
        final Func<String, SlackSession> sess) {
        this.farm = frm;
        this.posted = ptd;
        this.joined = new ReLogged<>(
            new ReInvite()
        );
        this.slackssess = new UncheckedFunc<>(
            (token) -> new RealSkSession(sess.apply(token))
        );
    }

    /**
     * Refresh all connections for all bots.
     * @throws IOException If fails
     */
    public void refresh() throws IOException {
        synchronized (this.farm) {
            final Bots bots = new Bots(this.farm).bootstrap();
            final Collection<String> tokens = new HashSet<>(0);
            for (final Map.Entry<String, String> bot : bots.tokens()) {
                if (!this.sessions().containsKey(bot.getKey())) {
                    try {
                        this.sessions().put(
                            bot.getKey(),
                            this.start(bot.getValue())
                        );
                    } catch (final IOException ex) {
                        Logger.warn(
                            this, "Can't connect to %s/%s: %s",
                            bots.name(bot.getKey()),
                            bot.getKey(), ex.getLocalizedMessage()
                        );
                        continue;
                    }
                }
                tokens.add(bot.getKey());
            }
            for (final String bid : this.sessions().keySet()) {
                if (!tokens.contains(bid)) {
                    this.sessions().remove(bid);
                }
            }
        }
        Logger.info(this, "Slack radar refreshed");
    }

    @Override
    public void close() throws IOException {
        for (final SkSession session : this.sessions().values()) {
            session.disconnect();
        }
    }

    /**
     * Create a session.
     * @param token Token
     * @return The session
     * @throws IOException If fails
     */
    private SkSession start(final String token) throws IOException {
        final SkSession ssn = this.slackssess.apply(token);
        ssn.connect();
        Logger.info(
            this, "Slack connected as @%s/%s to %s",
            ssn.persona().getUserName(),
            ssn.persona().getId(),
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

    /**
     * Sessions.
     * @return Sessions
     */
    private Map<String, SkSession> sessions() {
        return new ExtSlack(this.farm).value();
    }

}
