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
package com.zerocracy.stk.pm.in.orders;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.hr.Roles;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Find a performer and suggest an order creation.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class StkConfide implements Stakeholder {

    @Override
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final Wbs wbs = new Wbs(project);
        final Orders orders = new Orders(project);
        final Directives dirs = new Directives();
        for (final String job : wbs.iterate()) {
            if (orders.assigned(job)) {
                dirs.append(StkConfide.assign(project, job));
            }
        }
        return dirs;
    }

    /**
     * Assign one job.
     * @param project Project
     * @param job The job
     * @return Dirs or empty
     * @throws IOException If fails
     */
    private static Iterable<Directive> assign(final Project project,
        final String job) throws IOException {
        final Roles roles = new Roles(project);
        final List<String> logins = roles.findByRole("DEV");
        Collections.shuffle(logins);
        final Iterable<Directive> dirs;
        if (logins.isEmpty()) {
            dirs = Collections.emptyList();
        } else {
            final String login = logins.get(0);
            dirs = new ClaimOut()
                .type("pm.in.orders.start")
                .param("job", job)
                .param("login", login);
        }
        return dirs;
    }

}
