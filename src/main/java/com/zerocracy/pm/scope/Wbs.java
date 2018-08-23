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
package com.zerocracy.pm.scope;

import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import org.cactoos.time.DateAsText;
import org.cactoos.time.DateOf;
import org.xembly.Directives;

/**
 * WBS.
 *
 * <p>The WBS is a hierarchical decomposition of the total scope
 * of work to be carried out by the project team to accomplish
 * the project objectives and create the required deliverables.
 * The WBS organizes and defines the total scope of the project,
 * and represents the work specified in the current approved
 * project scope statement.
 *
 * @since 1.0
 * @todo #1159:30min Modify add method to accept user that created the given
 *  Wbs item and use it in stakeholders (and tests). All Wbs items should have
 *  an author (see zerocracy/datum#383).
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Wbs {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Wbs(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Wbs bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/scope/wbs");
        }
        return this;
    }

    /**
     * Add job to WBS.
     * @param job The job to add
     * @throws IOException If fails
     */
    public void add(final String job) throws IOException {
        if (this.exists(job)) {
            throw new SoftException(
                new Par("Job %s is already in scope").say(job)
            );
        }
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).modify(
                new Directives()
                    .xpath(String.format("/wbs[not(job[@id='%s'])]", job))
                    .strict(1)
                    .add("job")
                    .attr("id", job)
                    .add("role").set("DEV").up()
                    .add("created").set(new DateAsText().asString())
            );
        }
    }

    /**
     * Remove job from WBS.
     * @param job The job to remove
     * @throws IOException If fails
     */
    public void remove(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s was not in scope").say(job)
            );
        }
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).modify(
                new Directives().xpath(Wbs.xpath(job)).strict(1).remove()
            );
        }
    }

    /**
     * This job exists in WBS?
     * @param job The job to check
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String job) throws IOException {
        try (final Item wbs = this.item()) {
            return !new Xocument(wbs.path()).nodes(Wbs.xpath(job)).isEmpty();
        }
    }

    /**
     * List all jobs.
     * @return List of all jobs
     * @throws IOException If fails
     */
    public Collection<String> iterate() throws IOException {
        try (final Item wbs = this.item()) {
            return new Xocument(wbs.path()).xpath("/wbs/job/@id");
        }
    }

    /**
     * Set job role.
     * @param job The job to add
     * @param role The role
     * @throws IOException If fails
     */
    public void role(final String job, final String role) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s doesn't exist, can't set role").say(job)
            );
        }
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).modify(
                new Directives()
                    .xpath(String.format("/wbs/job[@id='%s']/role", job))
                    .set(role)
            );
        }
    }

    /**
     * Get job role.
     * @param job The job to add
     * @return The role
     * @throws IOException If fails
     */
    public String role(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s doesn't exist, can't get role").say(job)
            );
        }
        try (final Item wbs = this.item()) {
            return new Xocument(wbs.path()).xpath(
                String.format("/wbs/job[@id='%s']/role/text()", job)
            ).get(0);
        }
    }

    /**
     * Get job creating time.
     * @param job The job to add
     * @return The time when it was added to WBS
     * @throws IOException If fails
     */
    public Date created(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par("Job %s doesn't exist, can't check date").say(job)
            );
        }
        try (final Item wbs = this.item()) {
            return new DateOf(
                new Xocument(wbs.path()).xpath(
                    String.format("/wbs/job[@id='%s']/created/text()", job)
                ).get(0)
            ).value();
        }
    }

    /**
     * XPath to find a job.
     * @param job The job
     * @return XPath
     */
    private static String xpath(final String job) {
        return String.format("/wbs/job[@id='%s']", job);
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("wbs.xml");
    }

}
