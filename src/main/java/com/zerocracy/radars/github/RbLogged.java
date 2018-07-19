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
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * Rebound that logs.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbLogged implements Rebound {

    /**
     * Original reaction.
     */
    private final Rebound origin;

    /**
     * Ctor.
     * @param rtn Reaction
     */
    public RbLogged(final Rebound rtn) {
        this.origin = rtn;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final long start = System.currentTimeMillis();
        final String answer = this.origin.react(farm, github, event);
        Logger.info(
            this,
            "GitHub event, %s, %s, %s, %[ms]s",
            RbLogged.action(event),
            RbLogged.repository(event),
            RbLogged.issue(event),
            System.currentTimeMillis() - start
        );
        return answer;
    }

    /**
     * Action.
     * @param json JSON event
     * @return Action
     */
    private static String action(final JsonObject json) {
        final String text;
        if (json.containsKey("action")) {
            text = String.format("[%s]", json.getString("action"));
        } else {
            text = "no action";
        }
        return text;
    }

    /**
     * Repository.
     * @param json JSON event
     * @return Repository
     */
    private static String repository(final JsonObject json) {
        final String text;
        if (json.containsKey("repository")) {
            text = json.getJsonObject("repository").getString("full_name");
        } else {
            text = "no repo";
        }
        return text;
    }

    /**
     * Repository.
     * @param json JSON event
     * @return Repository
     */
    private static String issue(final JsonObject json) {
        final String text;
        if (json.containsKey("issue")) {
            text = String.format(
                "I:#%d",
                json.getJsonObject("issue").getInt("number")
            );
        } else if (json.containsKey("pull_request")) {
            text = String.format(
                "PR:#%d",
                json.getJsonObject("pull_request").getInt("number")
            );
        } else {
            text = "no issue";
        }
        return text;
    }
}
