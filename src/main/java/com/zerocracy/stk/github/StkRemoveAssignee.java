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
package com.zerocracy.stk.github;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.radars.github.Job;
import java.io.IOException;
import java.util.Collections;
import org.xembly.Directive;

/**
 * Remove assignee in GitHub.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class StkRemoveAssignee implements Stakeholder {

    /**
     * Github client.
     */
    private final Github github;

    /**
     * Ctor.
     * @param ghb Github client
     */
    public StkRemoveAssignee(final Github ghb) {
        this.github = ghb;
    }

    @Override
    public Iterable<Directive> process(final Project project, final XML xml)
        throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final Issue.Smart issue = new Issue.Smart(
            new Job.Issue(this.github, claim.param("job"))
        );
        if (issue.json().isNull("assignee")) {
            Logger.info(
                this, "Issue %s#%d doesn't have an assignee",
                issue.repo().coordinates(), issue.number()
            );
        } else {
            issue.assign("");
            Logger.info(
                this, "Issue %s#%d lost an assignee",
                issue.repo().coordinates(), issue.number()
            );
        }
        return Collections.emptyList();
    }

}
