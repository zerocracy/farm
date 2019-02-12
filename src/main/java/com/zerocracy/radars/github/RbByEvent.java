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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.zerocracy.Farm;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * Rebound by Github event name.
 * <p>
 *     Github event header should be added to {@code _0crat_github_event}
 *     JSON field. Event name is not case sensitive.
 * </p>
 *
 * @since 1.0
 */
public final class RbByEvent implements Rebound {

    /**
     * Origin.
     */
    private final Rebound rebound;

    /**
     * Event.
     */
    private final String evt;

    /**
     * Ctor.
     * @param rebound Origin rebound
     * @param event Event name
     */
    public RbByEvent(final Rebound rebound, final String event) {
        this.rebound = rebound;
        this.evt = event;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final boolean matched = event.getString("_0crat_github_event", "")
            .equalsIgnoreCase(this.evt);
        final String reaction;
        if (matched) {
            reaction = this.rebound.react(farm, github, event);
        } else {
            reaction = String.format("We're not interested (%s)", this.evt);
        }
        return reaction;
    }
}
