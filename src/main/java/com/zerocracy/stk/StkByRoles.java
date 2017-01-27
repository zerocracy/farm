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
package com.zerocracy.stk;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.hr.Roles;
import java.io.IOException;
import java.util.Arrays;
import org.xembly.Directive;

/**
 * Stakeholder that works only if the person belongs to the given roles.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkByRoles implements Stakeholder {

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
     * @param list List of roles
     * @param stk Original stakeholder
     */
    public StkByRoles(final Stakeholder stk, final String... list) {
        this(stk, Arrays.asList(list));
    }

    /**
     * Ctor.
     * @param list List of roles
     * @param stk Original stakeholder
     */
    public StkByRoles(final Stakeholder stk, final Iterable<String> list) {
        this.roles = list;
        this.origin = stk;
    }

    @Override
    public String term() {
        return String.format("(%s) and author", this.origin.term());
    }

    @Override
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final Iterable<Directive> dirs;
        if (this.has(project, xml)) {
            dirs = this.origin.process(project, xml);
        } else {
            dirs = new ClaimIn(xml).reply(
                String.format(
                    "You can't do that, unless you have one of these roles: %s",
                    String.join(", ", this.roles)
                )
            );
        }
        return dirs;
    }

    /**
     * Has one of that required roles.
     * @param project Project
     * @param xml Claim
     * @return TRUE if he has
     * @throws IOException If fails
     */
    private boolean has(final Project project,
        final XML xml) throws IOException {
        final Roles rls = new Roles(project).bootstrap();
        final String login = new ClaimIn(xml).author();
        boolean has = false;
        for (final String role : this.roles) {
            if (rls.hasRole(login, role)) {
                has = true;
                break;
            }
        }
        return has;
    }

}
