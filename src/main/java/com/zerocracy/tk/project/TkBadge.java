/**
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
package com.zerocracy.tk.project;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsText;
import org.takes.rs.RsWithStatus;

/**
 * Project badge.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @todo #1168:30min TkBadge is temporary disabled because of high load.
 *  There are a lot of requests to project badges, each request trigger
 *  SyncFarm lock acquire, which is broken right now.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkBadge implements TkRegex {
    @Override
    public Response act(final RqRegex req) throws IOException {
        return new RsWithStatus(
            new RsText("badges are temporary disabled"),
            HttpURLConnection.HTTP_UNAVAILABLE
        );
    }
}
