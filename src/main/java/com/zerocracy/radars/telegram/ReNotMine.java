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
package com.zerocracy.radars.telegram;

import com.zerocracy.jstk.Farm;
import java.io.IOException;

/**
 * React if not mine message.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.15
 */
final class ReNotMine implements Reaction {

    /**
     * Origin.
     */
    private final Reaction origin;

    /**
     * Ctor.
     * @param origin Reaction
     */
    ReNotMine(final Reaction origin) {
        this.origin = origin;
    }

    @Override
    public boolean react(
        final Farm farm,
        final TmSession session,
        final TmRequest request
    ) throws IOException {
        return !session.botname().equals(request.sender())
            && this.origin.react(farm, session, request);
    }
}
