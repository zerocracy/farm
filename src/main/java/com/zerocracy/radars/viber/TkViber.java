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
package com.zerocracy.radars.viber;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithStatus;

/**
 * Viber webhook entry point.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @since 0.22
 * @todo #939:30min Implement TkViber. We should be able to receive Viber
 *  webhook requests and react to them accordingly. In the case of "message"
 *  events, we should route them to ReProfile. For the rest we should at
 *  least be able to return a 200 OK response to Viber. When fully implemented
 *  this should be added to Main.java so that it can be called. See
 *  https://developers.viber.com/docs/api/rest-bot-api/
 */
public final class TkViber implements Take {

    @Override
    public Response act(final Request request) throws IOException {
        return new RsWithStatus(
            new RsWithBody("Not yet implemented."),
            HttpURLConnection.HTTP_NOT_IMPLEMENTED
        );
    }
}
