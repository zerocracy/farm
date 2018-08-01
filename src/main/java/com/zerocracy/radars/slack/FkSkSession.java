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
import com.ullink.slack.simpleslackapi.SlackTeam;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelJoinedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;
import java.io.IOException;

/**
 * Fake {@link SkSession}.
 * @since 0.28
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class FkSkSession implements SkSession {
    @Override
    public SlackChannel channel(final String id) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public SlackUser user(final String id) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void send(final SlackChannel channel, final String message) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void send(final SlackUser user, final String message) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public boolean hasChannel(final String id) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public SlackTeam getTeam() {
        return new FkTeam();
    }

    @Override
    public void connect() throws IOException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void disconnect() throws IOException {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public SlackPersona persona() {
        return new FkPersona();
    }

    @Override
    public void addMessagePostedListener(
        final SlackMessagePostedListener listener
    ) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void addChannelJoinedListener(
        final SlackChannelJoinedListener listener
    ) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public SlackMessageHandle<SlackChannelReply> openDirectMessageChannel(
        final SlackUser user
    ) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void close() throws IOException {
        throw new IllegalStateException("Not implemented");
    }
}
