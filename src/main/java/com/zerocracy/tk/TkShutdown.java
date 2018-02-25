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

import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHeaders;
import org.takes.rs.RsEmpty;

/**
 * Shutdown the app.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @todo #297:30min App shutdown is not implemented.
 *  This take will be called by Rultor during deploy.
 *  We should stop all background threads, services, wait until they stopped
 *  and return 200-OK status to Rultor.
 */
public final class TkShutdown implements Take {
    /**
     * Properties.
     */
    private final Props props;

    /**
     * Ctor.
     * @param properties Properties.
     */
    public TkShutdown(final Props properties) {
        this.props = properties;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String hdr = new RqHeaders.Smart(new RqHeaders.Base(req))
            .single("X-Auth", "");
        if (!hdr.equals(this.props.get("//shutdown/header", "test"))) {
            throw new RsForward(
                new RsParFlash(
                    "You are not allowed to shutdown",
                    Level.WARNING
                )
            );
        }
        return new RsEmpty();
    }
}
