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
package com.zerocracy.pm.in;

import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;
import org.cactoos.collection.CollectionOf;
import org.xembly.Directives;

/**
 * Job impediments.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Impediments {
    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt A project
     */
    public Impediments(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Impediments bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pm/in/impediments");
        }
        return this;
    }

    /**
     * Register an impediment.
     * @param job A job to register
     * @param reason Impediment reason
     * @throws IOException If fails
     */
    public void register(final String job, final String reason)
        throws IOException {
        final Wbs wbs = new Wbs(this.project).bootstrap();
        if (!wbs.exists(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not in scope, can't put it on hold"
                ).say(job)
            );
        }
        if ("REV".equals(wbs.role(job))) {
            throw new SoftException(
                new Par(
                    "It's a code review job %s, can't put it on hold"
                ).say(job)
            );
        }
        if (!new Orders(this.project).bootstrap().assigned(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not assigned, can't put it on hold"
                ).say(job)
            );
        }
        if (new CollectionOf<>(this.jobs()).contains(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is already on hold"
                ).say(job)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/impediments")
                    .add("order")
                    .attr("id", job)
                    .add("impediment")
                    .attr("type", "unknown")
                    .set(new Par.ToText(reason).toString())
                    .up()
                    .up()
            );
        }
    }

    /**
     * Remove a job's impediment.
     * @param job The job for which we remove the impediment.
     * @throws IOException If something goes wrong.
     */
    public void remove(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not on hold, no impediment to remove"
                ).say(job)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(Impediments.order(job))
                    .strict(1)
                    .remove()
            );
        }
    }

    /**
     * The impediment exists?
     * @param job The job
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists(final String job) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                Impediments.order(job)
            ).isEmpty();
        }
    }

    /**
     * Iterate all jobs with impediments.
     * @return Job ids
     * @throws IOException If fails
     */
    public Iterable<String> jobs() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .xpath("/impediments/order/@id");
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("impediments.xml");
    }

    /**
     * Construct the pull path to the given job.
     * @param job The job
     * @return The full path to the given job
     */
    private static String order(final String job) {
        return String.format("/impediments/order[@id='%s']", job);
    }
}
