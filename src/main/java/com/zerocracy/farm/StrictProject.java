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
package com.zerocracy.farm;

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * PMO project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class StrictProject implements Project {

    /**
     * Files that are allowed in PMO.
     */
    private static final Pattern PMO = Pattern.compile(
        String.join(
            "|",
            "(claims\\.xml)",
            "(catalog\\.xml)",
            "(bots\\.xml)",
            "(people\\.xml)"
        )
    );

    /**
     * Files that are allowed in a regular project.
     */
    private static final Pattern PROJECT = Pattern.compile(
        String.join(
            "|",
            "(claims\\.xml)",
            "(roles\\.xml)",
            "(wbs\\.xml)"
        )
    );

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Is it PMO?
     */
    private final boolean ispmo;

    /**
     * Prefix.
     */
    private final String prefix;

    /**
     * Ctor.
     * @param pkt Project
     * @param pmo Is it PMO?
     * @param pfx Prefix
     */
    StrictProject(final Project pkt, final boolean pmo, final String pfx) {
        this.origin = pkt;
        this.ispmo = pmo;
        this.prefix = pfx;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        if (this.ispmo && !StrictProject.PMO.matcher(file).matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "File \"%s\" is not accessible in \"%s\"",
                    file, this.prefix
                )
            );
        }
        if (!this.ispmo && !StrictProject.PROJECT.matcher(file).matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "File \"%s\" is not allowed in project \"%s\"",
                    file, this.prefix
                )
            );
        }
        return this.origin.acq(file);
    }

}
