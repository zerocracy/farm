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

import com.zerocracy.jstk.Farm;
import com.zerocracy.radars.telegram.Reaction;
import com.zerocracy.radars.telegram.RsText;
import com.zerocracy.radars.telegram.TmRequest;
import com.zerocracy.radars.telegram.TmSession;
import java.io.IOException;
import org.cactoos.Text;

/**
 * Reply with some text.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.17
 */
public final class FkTextReaction implements Reaction {

    /**
     * Text to reply.
     */
    private final Text text;

    /**
     * Ctor.
     * @param msg Text to reply
     */
    public FkTextReaction(final Text msg) {
        this.text = msg;
    }

    @Override
    public boolean react(
        final Farm farm,
        final TmSession session,
        final TmRequest request
    ) throws IOException {
        session.reply(new RsText(this.text));
        return true;
    }
}
