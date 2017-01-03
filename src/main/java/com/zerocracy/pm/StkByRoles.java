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
package com.zerocracy.pm;

import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.hr.Roles;
import java.io.IOException;

/**
 * Stakeholder that works only if the person belongs to the given roles.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkByRoles implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Person.
     */
    private final Person person;

    /**
     * Roles to allow.
     */
    private final Iterable<String> roles;

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Ctor.
     * @param pkt Project
     * @param prn Person
     * @param list List of roles
     * @param stk Original stakeholder
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public StkByRoles(final Project pkt, final Person prn,
        final Iterable<String> list, final Stakeholder stk) {
        this.project = pkt;
        this.person = prn;
        this.roles = list;
        this.origin = stk;
    }

    @Override
    public void work() throws IOException {
        if (this.has()) {
            this.origin.work();
        } else {
            this.person.say(
                String.format(
                    "You can't do that, unless you have one of these roles: %s",
                    String.join(", ", this.roles)
                )
            );
        }
    }

    /**
     * Has one of that required roles.
     * @return TRUE if he has
     * @throws IOException If fails
     */
    private boolean has() throws IOException {
        final Roles rls = new Roles(this.project);
        final String name = this.person.name();
        boolean has = false;
        for (final String role : this.roles) {
            if (rls.hasRole(name, role)) {
                has = true;
                break;
            }
        }
        return has;
    }
}
