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
import javax.json.JsonObject;
import org.cactoos.Func;
import org.cactoos.func.AsyncFunc;

/**
 * Rebound that works in background.
 *
 * @since 1.0
 */
public final class RbAsync implements Rebound {

    /**
     * Original reaction.
     */
    private final Rebound origin;

    /**
     * Ctor.
     * @param rtn Reaction
     */
    public RbAsync(final Rebound rtn) {
        this.origin = rtn;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) {
        new AsyncFunc<>(
            (Func<JsonObject, String>) input -> this.origin.react(
                farm, github, input
            )
        ).exec(event);
        return "We will process it soon, thanks!";
    }

}
