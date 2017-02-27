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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Brigade of stakeholders.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class Brigade {

    /**
     * Full list.
     */
    private final Collection<Stakeholder> list;

    /**
     * Ctor.
     */
    public Brigade() {
        this(Collections.emptyList());
    }

    /**
     * Ctor.
     * @param lst List of stakeholders
     */
    public Brigade(final Collection<Stakeholder> lst) {
        this.list = lst;
    }

    /**
     * Find Groovy stakeholders in a directory.
     * @param path Directory to search in
     * @throws IOException If fails
     */
    public Brigade append(final Path path) throws IOException {
        final List<Stakeholder> sum = new LinkedList<>();
        sum.addAll(this.list);
        Files.walk(path)
            .filter(file -> file.toFile().isFile())
            .filter(file -> file.toString().endsWith(".groovy"))
            .map(GroovyStakeholder::new)
            .forEach(sum::add);
        return new Brigade(sum);
    }

    /**
     * Process this claim.
     * @param project Project
     * @param xml XML to process
     * @throws IOException If fails
     */
    public void process(final Project project, final XML xml)
        throws IOException {
        for (final Stakeholder stk : this.list) {
            stk.process(project, xml);
        }
    }

}
