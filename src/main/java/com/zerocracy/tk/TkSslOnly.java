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
package com.zerocracy.tk;

import java.io.IOException;
import java.net.URI;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rq.RqHref;
import org.takes.rs.RsRedirect;

/**
 * Redirect to SSL if it't not HTTPS.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkSslOnly implements Take {

    /**
     * Original take.
     */
    private final Take origin;

    /**
     * Ctor.
     * @param take Original take
     */
    public TkSslOnly(final Take take) {
        this.origin = take;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String href = new RqHref.Base(req).href().bare();
        final URI uri = URI.create(href);
        final Response answer;
        if ("http".equalsIgnoreCase(uri.getScheme())
            && "www.0crat.com".equalsIgnoreCase(uri.getHost())) {
            answer = new RsRedirect(
                href.replaceAll("^http", "https")
            );
        } else {
            answer = this.origin.act(req);
        }
        return answer;
    }

}
