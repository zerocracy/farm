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
import java.util.Base64;
import org.cactoos.io.BytesOf;
import org.cactoos.io.InputOf;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsEmpty;

/**
 * Kill web app.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.222
 */
@SuppressWarnings("PMD.DoNotCallSystemExit")
public final class TkKill implements Take {
    @Override
    public Response act(final Request req) throws IOException {
        if (
            "a21zZGc4OW5TRzg5bi0uIw==".equals(
                Base64.getEncoder().encodeToString(
                    new BytesOf(new InputOf(req.body())).asBytes()
                )
            )
            ) {
            System.exit(1);
        }
        return new RsEmpty();
    }
}
