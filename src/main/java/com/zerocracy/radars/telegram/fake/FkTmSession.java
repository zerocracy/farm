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
package com.zerocracy.radars.telegram.fake;

import com.zerocracy.radars.telegram.TmResponse;
import com.zerocracy.radars.telegram.TmSession;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Fake Telegram session.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 */
public final class FkTmSession implements TmSession {

    /**
     * All responses collected here.
     */
    private final List<TmResponse> rsp;

    /**
     * Ctor.
     */
    public FkTmSession() {
        this(new LinkedList<>());
    }

    /**
     * Ctor.
     * @param rsp Responses collection
     */
    public FkTmSession(final List<TmResponse> rsp) {
        this.rsp = rsp;
    }

    @Override
    public void reply(final TmResponse response) throws IOException {
        this.rsp.add(response);
    }

    @Override
    public String botname() throws IOException {
        return "fake";
    }

    /**
     * All responses in this session.
     * @return Response list
     */
    public List<TmResponse> responses() {
        return Collections.unmodifiableList(this.rsp);
    }
}
