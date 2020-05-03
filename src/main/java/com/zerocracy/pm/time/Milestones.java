/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.zerocracy.ItemXml;
import com.zerocracy.Project;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Milestones.
 *
 * @since 1.0
 */
public final class Milestones {

    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param project Project
     */
    public Milestones(final Project project) {
        this.project = project;
    }

    /**
     * Bootstrap it.
     * @return Itself
     */
    public Milestones bootstrap() {
        return this;
    }

    /**
     * Add a milestone.
     * @param milestone Milestone name (gh:Milestone-1)
     * @param time Milestone end time
     * @throws IOException If fails
     */
    public void add(final String milestone, final LocalDate time)
        throws IOException {
        this.item().update(
            new Directives()
                .xpath("/milestones")
                .add("milestone")
                .attr("id", milestone)
                .add("date")
                .set(
                    new DateAsText(
                        Date.from(
                            time.atStartOfDay()
                                .atZone(ZoneOffset.UTC)
                                .toInstant()
                        )
                    ).asString()
                )
                .up()
                .up()
        );
    }

    /**
     * Iterate all milestones.
     * @return Milestone names iterable
     * @throws IOException If fails
     */
    public Iterable<String> iterate() throws IOException {
        return this.item().xpath("/milestones/milestone/@id");
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private ItemXml item() throws IOException {
        return new ItemXml(
            this.project.acq("milestones.xml"),
            "pm/time/milestones"
        );
    }
}
