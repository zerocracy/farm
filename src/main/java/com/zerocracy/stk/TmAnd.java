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
package com.zerocracy.stk;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Arrays;

/**
 * Term for AND.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class TmAnd implements Term {

    /**
     * Terms.
     */
    private final Iterable<Term> terms;

    /**
     * Ctor.
     * @param list List of terms
     */
    public TmAnd(final Term... list) {
        this(Arrays.asList(list));
    }

    /**
     * Ctor.
     * @param list List of terms
     */
    public TmAnd(final Iterable<Term> list) {
        this.terms = list;
    }

    @Override
    public boolean fits(final Project project,
        final XML xml) throws IOException {
        boolean fits = true;
        for (final Term term : this.terms) {
            if (!term.fits(project, xml)) {
                fits = false;
                break;
            }
        }
        return fits;
    }

}
