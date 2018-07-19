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
import javax.json.JsonObject;

/**
 * React if it's in the action.
 *
 * @since 1.0
 */
public final class RbByActions implements Rebound {

    /**
     * Original reaction.
     */
    private final Rebound origin;

    /**
     * Actions.
     */
    private final Collection<String> actions;

    /**
     * Ctor.
     * @param rtn Reaction
     * @param acts Action
     */
    public RbByActions(final Rebound rtn, final String... acts) {
        this.origin = rtn;
        this.actions = Arrays.asList(acts);
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final String field = "action";
        final String answer;
        if (event.containsKey(field)
            && this.actions.contains(event.getString(field))) {
            answer = String.format(
                "%s: %s",
                event.getString(field),
                this.origin.react(farm, github, event)
            );
        } else {
            answer = String.format(
                "We're not interested (%s)",
                this.actions
            );
        }
        return answer;
    }
}
