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
package com.zerocracy.stk.pm.hr.roles;

import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Person;
import com.zerocracy.pm.hr.Roles;
import java.io.IOException;

/**
 * Assign role to a person.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkAssign implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Tube.
     */
    private final Person person;

    /**
     * Role.
     */
    private final String role;

    /**
     * Who to assign it to.
     */
    private final String target;

    /**
     * Ctor.
     * @param pkt Project
     * @param tbe Tube
     * @param rle Role to assign
     * @param who Who to assign to
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public StkAssign(final Project pkt, final Person tbe,
        final String rle, final String who) {
        this.project = pkt;
        this.person = tbe;
        this.role = rle;
        this.target = who;
    }

    @Override
    public void work() throws IOException {
        new Roles(this.project).assign(this.target, this.role);
        this.person.say(
            String.format(
                "Role \"%s\" assigned to \"%s\"",
                this.role,
                this.target
            )
        );
    }
}
