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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.zerocracy.Farm;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import javax.json.JsonObject;

/**
 * Rebound.
 * <p>
 * Reaction for <a href="https://developer.github.com/webhooks/">
 * GitHub web-hook</a>. It processes a
 * <a href="https://developer.github.com/webhooks/#payloads">payload</a>
 * of webhook and returns text which will be displayed in repository
 * on "webhooks" section.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public interface Rebound {

    /**
     * Do something with this JSON event.
     * @param farm Farm
     * @param github Github client
     * @param event JSON event
     * @return The answer to give back to GitHub
     * @throws IOException If fails
     */
    String react(Farm farm, Github github, JsonObject event) throws IOException;

    /**
     * Reactions chained.
     */
    final class Chain implements Rebound {
        /**
         * Reactions.
         */
        private final Iterable<Rebound> reactions;
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Iterable<Rebound> list) {
            this.reactions = list;
        }
        /**
         * Ctor.
         * @param list All reactions
         */
        public Chain(final Rebound... list) {
            this(Arrays.asList(list));
        }
        @Override
        public String react(final Farm farm, final Github github,
            final JsonObject event) throws IOException {
            final Collection<String> answers = new LinkedList<>();
            for (final Rebound rebound : this.reactions) {
                answers.add(rebound.react(farm, github, event));
            }
            return String.join("; ", answers);
        }
    }
}
