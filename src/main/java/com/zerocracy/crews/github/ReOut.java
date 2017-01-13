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

import com.jcabi.github.Comment;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.Person;
import com.zerocracy.stk.StkByRoles;
import com.zerocracy.stk.StkSafe;
import com.zerocracy.stk.pm.scope.wbs.StkOut;
import java.io.IOException;
import java.util.Arrays;

/**
 * Remove this GitHub issue from project scope.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class ReOut implements Reply {

    @Override
    public void react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        final Project project = new GhProject(farm, comment);
        final Person person = new GhPerson(farm, comment);
        farm.deploy(
            new StkSafe(
                person,
                new StkByRoles(
                    project,
                    person,
                    Arrays.asList("PO", "ARC"),
                    new StkOut(
                        project,
                        person,
                        new GhJob(comment.issue())
                    )
                )
            )
        );
    }

}
