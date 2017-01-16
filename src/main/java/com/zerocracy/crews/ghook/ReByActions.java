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
package com.zerocracy.crews.ghook;

import com.jcabi.github.Github;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.json.JsonObject;

/**
 * React if it's in the action.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
final class ReByActions implements Reaction {

    /**
     * Original reaction.
     */
    private final Reaction origin;

    /**
     * Actions.
     */
    private final Collection<String> actions;

    /**
     * Ctor.
     * @param rtn Reaction
     * @param acts Action
     */
    ReByActions(final Reaction rtn, final String... acts) {
        this.origin = rtn;
        this.actions = Arrays.asList(acts);
    }

    @Override
    public void react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        if (this.actions.contains(event.getString("action"))) {
            this.origin.react(farm, github, event);
        }
    }
}
