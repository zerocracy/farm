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

import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackTeam;
import com.ullink.slack.simpleslackapi.listeners.SlackChannelJoinedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Fake {@link SkSession}.
 *
 * @since 0.28
 * @todo #1544:30min Finish implementation of FkSkSession, FkTeam and
 *  FkPersona, fake classes implementing SkSession, SlackTeam and
 *  SlackPersona. Then use these classes to replace Mockito in tests.
 *  In particular, try to remove from SkSession any reference to the
 *  underlying com.ullink.slack library in the same way SkUser and
 *  SkChannel abstracts over SlackUser and SlackChannel.
 * @todo #1544:30min Remove the need for the ConfusingTernary
 *  suppress warning caused by the if/else in channel and user. A good
 *  solution would be not to return null and handle non-existence of
 *  a channel or a user in a different more OO way. This would also
 *  impact RealSkSession and its users.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ConfusingTernary"})
public final class FkSkSession implements SkSession {

    /**
     * Users.
     */
    private final Set<String> users;

    /**
     * Channels.
     */
    private final Set<String> channels;

    /**
     * Ctor.
     */
    public FkSkSession() {
        this(Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Ctor.
     *
     * @param users Existing users.
     * @param channels Existing channels.
     */
    public FkSkSession(final Set<String> users, final Set<String> channels) {
        this.users = users;
        this.channels = channels;
    }

    @Override
    public SkChannel channel(final String id) {
        final SkChannel channel;
        if (!this.channels.contains(id)) {
            channel =  null;
        } else {
            channel = new SkChannel() {
                @Override
                public void send(final String message) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return channel;
    }

    @Override
    public SkUser user(final String id) {
        final SkUser user;
        if (!this.users.contains(id)) {
            user = null;
        } else {
            user = new SkUser() {
                @Override
                public void send(final String message) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return user;
    }

    @Override
    public boolean hasChannel(final String id) {
        return this.channels.contains(id);
    }

    @Override
    public SlackTeam getTeam() {
        return new FkTeam();
    }

    @Override
    public void connect() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SlackPersona persona() {
        return new FkPersona();
    }

    @Override
    public void addMessagePostedListener(
        final SlackMessagePostedListener listener
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addChannelJoinedListener(
        final SlackChannelJoinedListener listener
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }
}
