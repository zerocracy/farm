/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.zold;

import com.jcabi.http.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.cactoos.Text;

/**
 * Zold error header.
 *
 * @since 1.0
 */
final class ZldError implements Text {

    /**
     * Response.
     */
    private final Response rsp;

    /**
     * Ctor.
     * @param rsp Response
     */
    ZldError(final Response rsp) {
        this.rsp = rsp;
    }

    @Override
    public String asString() {
        final List<String> hdr = this.rsp.headers()
            .getOrDefault("X-Zold-Error", Collections.emptyList());
        final String error;
        if (hdr.isEmpty()) {
            error = "unknown";
        } else {
            error = hdr.get(0);
        }
        return error;
    }

    /**
     * Raise error.
     * @param msg Message for exception
     * @throws IOException Will be thrown
     */
    public void raise(final String msg) throws IOException {
        throw new IOException(
            String.format(
                "%s; code=%d error=%s",
                msg, this.rsp.status(), this.asString()
            )
        );
    }
}
