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
import java.util.Arrays;
import java.util.Collections;
import org.xembly.Directive;

/**
 * Stakeholder that works only if condition is true.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class StkByTerm implements Stakeholder {

    /**
     * Original stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Term.
     */
    private final Term term;

    /**
     * Ctor.
     * @param stk Original stakeholder
     * @param list List of terms
     */
    public StkByTerm(final Stakeholder stk, final Term... list) {
        this(stk, Arrays.asList(list));
    }

    /**
     * Ctor.
     * @param stk Original stakeholder
     * @param list List of terms
     */
    public StkByTerm(final Stakeholder stk, final Iterable<Term> list) {
        this.origin = stk;
        this.term = new TmAnd(list);
    }

    @Override
    public Iterable<Directive> process(final Project project,
        final XML xml) throws IOException {
        final Iterable<Directive> dirs;
        if (this.term.fits(project, xml)) {
            dirs = this.origin.process(project, xml);
        } else {
            dirs = Collections.emptyList();
        }
        return dirs;
    }

}
