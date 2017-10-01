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
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.JsonObject;

/**
 * GitHub notifications listening crew.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class GithubFetch implements Runnable {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Reaction.
     */
    private final Reaction reaction;

    /**
     * Ctor.
     * @param frm Farm
     * @param ghb Github client
     * @param rtn Reaction
     */
    public GithubFetch(final Farm frm, final Github ghb, final Reaction rtn) {
        this.farm = frm;
        this.github = ghb;
        this.reaction = rtn;
    }

    @Override
    public void run() {
        try {
            this.fetch();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Fetch next portion of notifications.
     * @throws IOException If fails
     */
    private void fetch() throws IOException {
        final Request req = this.github.entry()
            .uri().path("/notifications").back();
        final Iterable<JsonObject> events =
            new RtPagination<>(req, RtPagination.COPYING);
        for (final JsonObject event : events) {
            this.reaction.react(this.farm, event);
        }
        req.method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
    }
}
