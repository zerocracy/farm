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

import com.ullink.slack.simpleslackapi.events.SlackEvent;
import com.zerocracy.Farm;
import java.io.IOException;

/**
 * React to Slack message.
 *
 * @param <T> Type of event
 * @since 1.0
 */
public interface Reaction<T extends SlackEvent> {

    /**
     * Do something about it.
     * @param farm Farm
     * @param event Event just happened
     * @param session Session
     * @return TRUE if reacted
     * @throws IOException If fails on I/O
     */
    boolean react(Farm farm, T event, SkSession session) throws IOException;

    /**
     * Reactions chained.
     * @param <T> Type of event
     */
    final class Chain<T extends SlackEvent> implements Reaction<T> {
        /**
         * Reactions.
         */
        private final Iterable<Reaction<T>> reactions;
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Iterable<Reaction<T>> list) {
            this.reactions = list;
        }
        @Override
        public boolean react(final Farm farm, final T event,
            final SkSession session) throws IOException {
            boolean done = false;
            for (final Reaction<T> reaction : this.reactions) {
                done = reaction.react(farm, event, session);
                if (done) {
                    break;
                }
            }
            return done;
        }
    }

}
