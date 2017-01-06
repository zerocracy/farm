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
import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;

/**
 * Bootstrap a project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Bootstrap implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Person.
     */
    private final Person person;

    /**
     * Ctor.
     * @param pkt Project
     * @param prn Person
     */
    public Bootstrap(final Project pkt, final Person prn) {
        this.project = pkt;
        this.person = prn;
    }

    @Override
    public void work() throws IOException {
        this.scope();
        this.staff();
    }

    /**
     * Bootstrap Scope.
     * @throws IOException If fails
     */
    private void scope() throws IOException {
        new Wbs(this.project).bootstrap();
    }

    /**
     * Bootstrap HR.
     * @throws IOException If fails
     */
    private void staff() throws IOException {
        final Roles roles = new Roles(this.project);
        roles.bootstrap();
        final String role = "PO";
        if (roles.hasRole(this.person.uid(), role)) {
            this.person.say(
                String.join(
                    " ",
                    "This project seems to be under my management already.",
                    "If you have questions, try this link:",
                    "http://www.0crat.com/help.html or email me."
                )
            );
        } else {
            roles.assign(this.person.uid(), role);
            this.person.say(
                String.join(
                    " ",
                    "I'm ready to manage a project.",
                    "When you're ready, you can start giving me instructions,",
                    "always prefixing your messages with my uid.",
                    "If you need help, start here:",
                    "http://www.0crat.com/help.html"
                )
            );
        }
    }
}
