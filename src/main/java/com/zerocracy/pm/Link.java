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

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Attach a resource to the project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Link implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Person.
     */
    private final Person person;

    /**
     * LINK rel.
     */
    private final String rel;

    /**
     * LINK href.
     */
    private final String href;

    /**
     * Ctor.
     * @param pkt Project
     * @param prn Person
     * @param ref Reference
     * @param hrf HREF
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Link(final Project pkt, final Person prn, final String ref,
        final String hrf) {
        this.project = pkt;
        this.person = prn;
        this.rel = ref;
        this.href = hrf;
    }

    @Override
    public void work() throws IOException {
        try (final Item item = this.project.acq("../catalog.xml")) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/catalog/project")
                    .addIf("links")
                    .add("link")
                    .attr("rel", this.rel)
                    .attr("href", this.href)
            );
        }
        this.person.say(
            String.format(
                "Done, the project is linked with rel=`%s` and href=`%s`",
                this.rel, this.href
            )
        );
    }
}
