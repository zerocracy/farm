/*
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
package com.zerocracy.pm.time;

import com.jcabi.xml.XML;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import org.xembly.Directives;

/**
 * A Project's precedences. There are 4 types of precedence: finish-to-start,
 * start-to-start, finish-to-finish, and start-to-finish. More about it
 * <a href="https://project-management-knowledge.com/definitions/p/precedence-diagramming-method/">here</a>.
 *
 * @since 1.0
 */
@SuppressWarnings({"PMD.UseObjectForClearerAPI", "PMD.AvoidDuplicateLiterals"})
public final class Precedences {

    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param project Project
     */
    public Precedences(final Project project) {
        this.project = project;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Precedences bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pm/time/precedences");
        }
        return this;
    }

    /**
     * Add a precedence. The default type of the predecessor and successor are
     * "job".
     * @param type Type of the precedence (one of the four types).
     * @param predecessor The predecessor.
     * @param successor The successor.
     * @throws IOException If fails
     */
    public void add(
        final String type, final String predecessor,
        final String successor
    ) throws IOException {
        final String attr = "job";
        this.add(type, predecessor, attr, successor, attr);
    }

    /**
     * Add a precedence.
     * @param type Type of the precedence (one of the four types).
     * @param predecessor The predecessor.
     * @param prectype The predecessor's type.
     * @param successor The successor.
     * @param suctype The successor's type.
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (10 lines)
     */
    public void add(
        final String type,
        final String predecessor,
        final String prectype,
        final String successor,
        final String suctype
    ) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/precedences")
                    .add("precedence")
                    .add("type")
                    .set(type)
                    .up()
                    .add("predecessor")
                    .attr("type", prectype)
                    .set(predecessor)
                    .up()
                    .add("successor")
                    .attr("type", suctype)
                    .set(successor)
            );
        }
    }

    /**
     * Iterate all precedences.
     * @return Precedeces' XML iterable/
     * @throws IOException If fails
     */
    public Iterable<XML> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .nodes("/precedences/precedence");
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("precedences.xml");
    }
}
