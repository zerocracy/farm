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
package com.zerocracy.radars.telegram.fake;

import com.zerocracy.radars.telegram.TmRequest;

/**
 * Fake telegram request.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 */
public final class FkTmRequest implements TmRequest {

    /**
     * Sender.
     */
    private final String snd;

    /**
     * Text.
     */
    private final String txt;

    /**
     * Chat id.
     */
    private final long id;

    /**
     * Ctor.
     * @param sender Sender
     * @param text Text
     * @param chat Chat id
     */
    public FkTmRequest(final String sender, final String text,
        final long chat) {
        this.snd = sender;
        this.txt = text;
        this.id = chat;
    }

    @Override
    public String sender() {
        return this.snd;
    }

    @Override
    public String text() {
        return this.txt;
    }

    @Override
    public long chat() {
        return this.id;
    }
}
