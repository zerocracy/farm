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
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.Farm;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.LinkedList;
import javax.json.JsonObject;

/**
 * GitHub notifications listening crew.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbOnComment implements Rebound {

    /**
     * Reaction.
     */
    private final Reaction reaction;

    /**
     * Ctor.
     * @param rtn Reaction
     */
    public RbOnComment(final Reaction rtn) {
        this.reaction = rtn;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject json) throws IOException {
        final Request req = github.entry()
            .uri().path("/notifications").back();
        final Iterable<JsonObject> events =
            new RtPagination<>(req, RtPagination.COPYING);
        final Collection<String> messages = new LinkedList<>();
        for (final JsonObject event : events) {
            messages.add(this.reaction.react(farm, event));
        }
        req.method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
        return String.format(
            "%d GitHub events seen: %s",
            messages.size(), String.join(", ", messages)
        );
    }
}
