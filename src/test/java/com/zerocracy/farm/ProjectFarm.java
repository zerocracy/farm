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
package com.zerocracy.farm;

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import java.util.Collections;
import org.cactoos.iterable.IterableOf;

/**
 * Test farm with single project.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 */
public final class ProjectFarm implements Farm {
    /**
     * A project.
     */
    private final Project proj;

    /**
     * Project's xpath.
     */
    private final String xpth;

    /**
     * Ctor.
     * @param proj A project
     * @param xpth Project's xpath
     */
    public ProjectFarm(final Project proj, final String xpth) {
        this.proj = proj;
        this.xpth = xpth;
    }

    @Override
    public Iterable<Project> find(final String xpath) {
        final Iterable<Project> ret;
        if (this.xpth.equals(xpath)) {
            ret = new IterableOf<>(this.proj);
        } else {
            ret = Collections.emptyList();
        }
        return ret;
    }
}
