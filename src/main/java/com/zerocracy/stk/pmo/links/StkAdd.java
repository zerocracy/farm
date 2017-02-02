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
package com.zerocracy.stk.pmo.links;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import org.xembly.Directive;

/**
 * Attach a resource to the project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkAdd implements Stakeholder {

    /**
     * Github.
     */
    private final Github github;

    /**
     * Ctor.
     * @param ghub Github
     */
    public StkAdd(final Github ghub) {
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final String pid = claim.param("project");
        final String rel = claim.param("rel");
        final String href = claim.param("href");
        if ("github".equals(rel)) {
            this.github.repos().get(
                new Coordinates.Simple(href)
            ).stars().star();
        }
        new Catalog(project).link(pid, rel, href);
        return claim.reply(
            String.format(
                "Done, the project is linked with rel=`%s` and href=`%s`.",
                rel, href
            )
        );
    }

}
