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
import java.io.IOException;
import java.util.Collections;
import org.xembly.Directive;

/**
 * Stakeholder that works only if this XPath is there.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class StkByXpath implements Stakeholder {

    /**
     * XPath to match.
     */
    private final String xpath;

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Ctor.
     * @param path Type to match
     * @param stk Original stakeholder
     */
    public StkByXpath(final String path, final Stakeholder stk) {
        this.xpath = path;
        this.origin = stk;
    }

    @Override
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final String query = String.format("/claim[%s]", this.xpath);
        final Iterable<Directive> dirs;
        if (xml.nodes(query).isEmpty()) {
            dirs = Collections.emptyList();
        } else {
            dirs = this.origin.process(project, xml);
        }
        return dirs;
    }

}
