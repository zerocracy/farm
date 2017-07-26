/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.json.JsonObject;
import org.cactoos.Text;
import org.cactoos.text.FormattedText;

/**
 * React for github issue labels.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.12
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
     * @param origin Origin
     * @param labels Labels
     */
    public RbByLabel(final Rebound origin, final String... labels) {
        this(origin, Arrays.asList(labels));
    }

    /**
     * Primary ctor.
     *
     * @param origin Origin
     * @param labels Labels
     */
    public RbByLabel(final Rebound origin, final Collection<String> labels) {
        this.origin = origin;
        this.labels = labels;
    }

    @Override
    public String react(
        final Farm farm,
        final Github github,
        final JsonObject event
    ) throws IOException {
        final Text reaction;
        if (event.containsKey(RbByLabel.FIELD)
            && this.labels.contains(event.getString(RbByLabel.FIELD))) {
            reaction = new FormattedText(
                "[%s]: %s",
                event.getString(RbByLabel.FIELD),
                this.origin.react(farm, github, event)
            );
        } else {
            reaction = new FormattedText(
                "We're not interested (%s)",
                this.labels
            );
        }
        return reaction.asString();
    }
}
