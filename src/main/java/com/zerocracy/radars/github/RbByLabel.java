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
import org.cactoos.Text;
import org.cactoos.text.FormattedText;

/**
 * React for github issue labels.
 *
 * @since 1.0
 * @link https://developer.github.com/v3/activity/events/types/#labelevent
 */
public final class RbByLabel implements Rebound {

    /**
     * Json label field.
     */
    private static final String FIELD = "label";

    /**
     * Origin.
     */
    private final Rebound origin;

    /**
     * Issue labels.
     */
    private final Collection<String> labels;

    /**
     * Ctor.
     *
     * @param rebound Origin
     * @param list Labels
     */
    public RbByLabel(final Rebound rebound, final String... list) {
        this(rebound, Arrays.asList(list));
    }

    /**
     * Primary ctor.
     *
     * @param rebound Origin
     * @param list Labels
     */
    public RbByLabel(final Rebound rebound, final Collection<String> list) {
        this.origin = rebound;
        this.labels = list;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Text reaction;
        if (event.containsKey(RbByLabel.FIELD)) {
            final String label = event.getJsonObject(RbByLabel.FIELD)
                .getString("name");
            if (this.labels.contains(label)) {
                reaction = new FormattedText(
                    "[%s]: %s",
                    label, this.origin.react(farm, github, event)
                );
            } else {
                reaction = new FormattedText("[%s]: not this one", label);
            }
        } else {
            reaction = new FormattedText(
                "We're not interested (%s)",
                this.labels
            );
        }
        return reaction.asString();
    }
}
