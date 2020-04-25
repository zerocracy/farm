/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackTeam;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelJoinedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.IOException;
import java.util.Optional;

/**
 * Real session in Slack.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class RealSkSession implements SkSession {

    /**
     * Original slack session.
     */
    private final SlackSession origin;

    /**
     * Ctor.
     * @param origin Original slack session
     */
    public RealSkSession(final SlackSession origin) {
        this.origin = origin;
    }

    @Override
    public SkChannel channel(final String id) {
        return Optional.ofNullable(this.origin.findChannelById(id))
            .map(RealSkChannel::new)
            .orElse(null);
    }

    @Override
    public SkUser user(final String id) {
        return Optional.ofNullable(this.origin.findUserById(id))
            .map(RealSkUser::new)
            .orElse(null);
    }

    @Override
    public boolean hasChannel(final String id) {
        boolean has = false;
        for (final SlackChannel channel : this.origin.getChannels()) {
            if (channel.getId().equals(id)) {
                has = true;
            }
        }
        return has;
    }

    @Override
    public SlackTeam getTeam() {
        return this.origin.getTeam();
    }

    @Override
    public void connect() throws IOException {
        this.origin.connect();
    }

    @Override
    public void disconnect() throws IOException {
        this.origin.disconnect();
    }

    @Override
    public SlackPersona persona() {
        return this.origin.sessionPersona();
    }

    @Override
    public void addMessagePostedListener(
        final SlackMessagePostedListener listener
    ) {
        this.origin.addMessagePostedListener(listener);
    }

    @Override
    public void addChannelJoinedListener(
        final SlackChannelJoinedListener listener
    ) {
        this.origin.addChannelJoinedListener(listener);
    }

    @Override
    public void close() throws IOException {
        this.origin.disconnect();
    }

    /**
     * Real user in Slack.
     *
     * @since 1.0
     */
    private final class RealSkUser implements SkUser {

        /**
         * Original slack user.
         */
        private final SlackUser origin;

        /**
         * Ctor.
         * @param origin Original slack user
         */
        RealSkUser(final SlackUser origin) {
            this.origin = origin;
        }

        @Override
        public void send(final String message) {
            RealSkSession.this.origin.sendMessage(
                RealSkSession.this.origin.openDirectMessageChannel(this.origin)
                    .getReply()
                    .getSlackChannel(),
                message
            );
        }
    }

    /**
     * Real channel in Slack.
     *
     * @since 1.0
     */
    private final class RealSkChannel implements SkChannel {

        /**
         * Original slack channel.
         */
        private final SlackChannel origin;

        /**
         * Ctor.
         * @param origin Original slack channel
         */
        RealSkChannel(final SlackChannel origin) {
            this.origin = origin;
        }

        @Override
        public void send(final String message) {
            RealSkSession.this.origin.sendMessage(this.origin, message);
        }
    }
}
