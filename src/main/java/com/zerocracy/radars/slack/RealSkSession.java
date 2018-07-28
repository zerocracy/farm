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

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackTeam;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelJoinedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;
import java.io.IOException;

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
    public SlackChannel channel(final String id) {
        return this.origin.findChannelById(id);
    }

    @Override
    public SlackUser user(final String id) {
        return this.origin.findUserById(id);
    }

    @Override
    public void send(final SlackChannel channel, final String message) {
        this.origin.sendMessage(channel, message);
    }

    @Override
    public void send(final SlackUser user, final String message) {
        this.origin.sendMessage(
            this.origin.openDirectMessageChannel(user)
                .getReply()
                .getSlackChannel(),
            message
        );
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
    public SlackMessageHandle<SlackChannelReply> openDirectMessageChannel(
        final SlackUser user
    ) {
        return this.origin.openDirectMessageChannel(user);
    }

    @Override
    public void close() throws IOException {
        this.origin.disconnect();
    }
}
