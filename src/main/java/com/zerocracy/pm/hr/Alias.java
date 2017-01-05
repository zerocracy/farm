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
package com.zerocracy.pm.hr;

import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Person;
import java.io.IOException;

/**
 * Add an alias to the user.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Alias implements Stakeholder {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Tube.
     */
    private final Person person;

    /**
     * REL.
     */
    private final String rel;

    /**
     * Href.
     */
    private final String href;

    /**
     * Ctor.
     * @param pkt Project
     * @param tbe Tube
     * @param ref Ref
     * @param hrf HREF
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public Alias(final Project pkt, final Person tbe,
        final String ref, final String hrf) {
        this.project = pkt;
        this.person = tbe;
        this.rel = ref;
        this.href = hrf;
    }

    @Override
    public void work() throws IOException {
        new People(this.project).link(this.person.name(), this.rel, this.href);
        this.person.say(
            String.format(
                "Alias added to \"%s\": rel=`%s`, href=`%s`",
                this.person.name(),
                this.rel,
                this.href
            )
        );
    }
}
