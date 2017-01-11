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
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackChannelJoined;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.zerocracy.crews.slack.profile.ReAlias;
import com.zerocracy.crews.slack.profile.ReRate;
import com.zerocracy.jstk.Crew;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pmo.Bots;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Slack listening crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SkCrew implements Crew {

    /**
     * When new message posted.
     */
    private static final Reaction<SlackMessagePosted> POSTED = new ReSafe(
        new ReLogged<>(
            new ReNotMine(
                new ReIfDirect(
                    new Reaction.Chain<>(
                        Arrays.asList(
                            new ReRegex("alias .*", new ReAlias()),
                            new ReRegex("rate .*", new ReRate()),
                            new ReRegex(".*", new ReSorry())
                        )
                    ),
                    new ReIfAddressed(
                        new Reaction.Chain<>(
                            Arrays.asList(
                                new ReRegex("hello|hi|hey", new ReHello()),
                                new ReRegex("bootstrap", new ReBootstrap()),
                                new ReRegex("wbs", new ReShowWbs()),
                                new ReRegex("roles", new ReShowRoles()),
                                new ReRegex("assign .*", new ReAssign()),
                                new ReRegex("resign .*", new ReResign()),
                                new ReRegex("link .*", new ReLink()),
                                new ReRegex(".*", new ReSorry())
                            )
                        )
                    )
                )
            )
        )
    );

    /**
     * When joined new channel.
     */
    private static final Reaction<SlackChannelJoined> JOINED = new ReLogged<>(
        new ReInvite()
    );

    /**
     * Slack sessions (Bot ID vs. Session).
     */
    private final Map<String, SlackSession> sessions =
        new HashMap<>(0);

    @Override
    public void deploy(final Farm farm) throws IOException {
        final Bots bots = new Bots(
            farm.find("@id='PMO'").iterator().next()
        );
        bots.bootstrap();
        final Collection<String> tokens = new HashSet<>(0);
        for (final Map.Entry<String, String> bot : bots.tokens()) {
            tokens.add(bot.getKey());
            if (this.sessions.containsKey(bot.getKey())) {
                continue;
            }
            this.sessions.put(bot.getKey(), this.start(farm, bot.getValue()));
        }
        for (final String bid : this.sessions.keySet()) {
            if (!tokens.contains(bid)) {
                this.sessions.remove(bid);
            }
        }
    }

    /**
     * Create a session.
     * @param farm The farm
     * @param token Token
     * @return The session
     * @throws IOException If fails
     */
    private SlackSession start(final Farm farm, final String token)
        throws IOException {
        final SlackSession ssn =
            SlackSessionFactory.createWebSocketSlackSession(token);
        ssn.connect();
        Logger.info(
            this, "Slack connected as @%s/%s",
            ssn.sessionPersona().getUserName(),
            ssn.sessionPersona().getId()
        );
        ssn.addMessagePostedListener(
            (event, sess) -> {
                try {
                    SkCrew.POSTED.react(farm, event, ssn);
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        );
        ssn.addChannelJoinedListener(
            (event, sess) -> {
                try {
                    SkCrew.JOINED.react(farm, event, ssn);
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        );
        return ssn;
    }

}
