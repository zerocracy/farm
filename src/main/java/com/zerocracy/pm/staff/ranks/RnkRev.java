/**
 * Copyright (c) 2016-2018 Zerocracy
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
package com.zerocracy.pm.staff.ranks;

import com.zerocracy.Project;
import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;
import java.util.Comparator;

/**
 * Give higher rank for 'REV' jobs.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.18.8
 */
public final class RnkRev implements Comparator<String> {
    /**
     * Role 'REV'.
     */
    private static final String REV = "REV";

    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param project A project
     */
    public RnkRev(final Project project) {
        this.project = project;
    }

    @SuppressWarnings("IfStatementWithTooManyBranches")
    @Override
    public int compare(final String left, final String right) {
        try {
            final Wbs wbs = new Wbs(this.project).bootstrap();
            return Boolean.compare(
                RnkRev.REV.equals(wbs.role(right)),
                RnkRev.REV.equals(wbs.role(left))
            );
        } catch (final IOException err) {
            throw new IllegalStateException(err);
        }
    }
}
