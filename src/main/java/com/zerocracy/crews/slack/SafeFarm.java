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
package com.zerocracy.crews.slack;

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Person;
import com.zerocracy.stk.StkSafe;
import java.io.IOException;

/**
 * Safe farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
final class SafeFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Person to complain to.
     */
    private final Person person;

    /**
     * Ctor.
     * @param frm Farm
     * @param prn Person
     */
    SafeFarm(final Farm frm, final Person prn) {
        this.origin = frm;
        this.person = prn;
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return this.origin.find(query);
    }

    @Override
    public void deploy(final Stakeholder stakeholder) throws IOException {
        this.origin.deploy(new StkSafe(this.person, stakeholder));
    }
}
