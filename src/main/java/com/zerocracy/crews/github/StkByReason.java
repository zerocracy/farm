/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.crews.github;

import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;

/**
 * Stakeholder by reason.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkByReason implements Stakeholder {

    /**
     * GitHub event.
     */
    private final Event event;

    /**
     * Reason.
     */
    private final String reason;

    /**
     * Stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Ctor.
     * @param evt Event in GitHub
     * @param rson Reason
     * @param stk Stakeholder
     */
    public StkByReason(final Event evt, final String rson,
        final Stakeholder stk) {
        this.event = evt;
        this.reason = rson;
        this.origin = stk;
    }

    @Override
    public void work() throws IOException {
        if (this.event.reason().equals(this.reason)) {
            this.origin.work();
        }
    }
}
