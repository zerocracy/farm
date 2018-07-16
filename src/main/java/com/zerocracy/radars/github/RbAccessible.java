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

import com.jcabi.aspects.Tv;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import javax.json.JsonObject;
import org.cactoos.text.SubText;
import org.takes.facets.forward.RsForward;

/**
 * Rebound that validates access to GitHub.
 *
 * @since 1.0
 */
public final class RbAccessible implements Rebound {

    /**
     * Original reaction.
     */
    private final Rebound origin;

    /**
     * Ctor.
     * @param rtn Reaction
     */
    public RbAccessible(final Rebound rtn) {
        this.origin = rtn;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final JsonObject obj = event.getJsonObject("repository");
        if (obj == null) {
            throw new RsForward(
                new RsParFlash(
                    "There is no repository information in this webhook",
                    Level.WARNING
                )
            );
        }
        final String repo = obj.getString("full_name");
        try {
            github.repos().get(new Coordinates.Simple(repo)).stars().star();
        } catch (final AssertionError ex) {
            final String self = github.users().self().login();
            Logger.warn(
                this, "%s is not accessible for @%s, can't process event",
                repo, self
            );
            throw new RsForward(
                new RsParFlash(
                    new Par("Repository %s is not accessible for @%s: %s").say(
                        repo, self,
                        new SubText(
                            ex.getLocalizedMessage(), 0, Tv.HUNDRED
                        ).asString()
                    ),
                    Level.WARNING
                )
            );
        }
        return this.origin.react(farm, github, event);
    }

}
