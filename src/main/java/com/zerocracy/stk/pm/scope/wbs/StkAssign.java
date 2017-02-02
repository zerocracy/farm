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
package com.zerocracy.stk.pm.scope.wbs;

import com.google.common.collect.Iterables;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;
import org.xembly.Directive;

/**
 * Assign a performer.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class StkAssign implements Stakeholder {

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final String job = claim.param("job");
        String login = claim.param("login");
        if ("me".equals(login)) {
            login = claim.author();
        }
        new Wbs(project).bootstrap().assign(job, login);
        return Iterables.concat(
            new ClaimOut()
                .type("pm.scope.wbs.assigned")
                .param("job", job)
                .param("login", login),
            claim.reply(
                String.format(
                    "Done, job `%s` assigned to @%s.",
                    job, login
                )
            )
        );
    }

}
