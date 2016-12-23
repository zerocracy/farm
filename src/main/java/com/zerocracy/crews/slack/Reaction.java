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

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Arrays;

/**
 * React to Slack message.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public interface Reaction {

    /**
     * Do something about it.
     * @param farm Farm
     * @param event Event just happened
     * @throws IOException If fails on I/O
     */
    void react(Farm farm, SlackMessagePosted event) throws IOException;

    /**
     * Reactions chained.
     */
    final class Chain implements Reaction {
        /**
         * Reactions.
         */
        private final Iterable<Reaction> reactions;
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Iterable<Reaction> list) {
            this.reactions = list;
        }
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Reaction... list) {
            this(Arrays.asList(list));
        }
        @Override
        public void react(final Farm farm, final SlackMessagePosted event)
            throws IOException {
            for (final Reaction reaction : this.reactions) {
                reaction.react(farm, event);
            }
        }
    }
}
