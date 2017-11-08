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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stakeholder that knows self status.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16.1
 */
final class StkWithStatus implements Stakeholder {

    /**
     * Origin stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Result memory.
     */
    private final AtomicBoolean res;

    /**
     * Ctor.
     * @param stk Origin stakeholder
     */
    StkWithStatus(final Stakeholder stk) {
        this.origin = stk;
        this.res = new AtomicBoolean();
    }

    @Override
    public void process(final Project project, final XML claim)
        throws IOException {
        this.origin.process(project, claim);
        this.res.set(true);
    }

    /**
     * Result status.
     * @return TRUE for success
     */
    public boolean status() {
        return this.res.get();
    }
}
