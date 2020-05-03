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
package com.zerocracy.pmo;

import com.zerocracy.Farm;
import com.zerocracy.ItemXml;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import org.xembly.Directives;

/**
 * Agenda of one person in one Project, tasks that
 * are assigned to them in that Project.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Agenda {

    /**
     * Project.
     */
    private final Project pmo;

    /**
     * Login of the person.
     */
    private final String login;

    /**
     * Time measure.
     */
    private final Clock clock;

    /**
     * Ctor.
     * @param farm The farm
     * @param user The user
     * @param clock Clock to use
     */
    Agenda(final Farm farm, final String user, final Clock clock) {
        this(new Pmo(farm), user, clock);
    }

    /**
     * Ctor.
     * @param farm The farm
     * @param user The user
     */
    public Agenda(final Farm farm, final String user) {
        this(new Pmo(farm), user, Clock.systemUTC());
    }

    /**
     * Ctor.
     * @param pkt PMO
     * @param user The user
     * @param clock Clock to use
     */
    private Agenda(final Pmo pkt, final String user, final Clock clock) {
        this.pmo = pkt;
        this.login = user;
        this.clock = clock;
    }

    /**
     * Ctor.
     * @param pkt PMO
     * @param user The user
     */
    public Agenda(final Pmo pkt, final String user) {
        this(pkt, user, Clock.systemUTC());
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Agenda bootstrap() throws IOException {
        return this;
    }

    /**
     * Return full list of jobs this person currently is working with.
     * @return List of job IDs
     * @throws IOException If fails
     */
    public Collection<String> jobs() throws IOException {
        return this.item().xpath("/agenda/order/@job");
    }

    /**
     * Return full list of jobs this person currently is working
     * in particular project.
     * @param pkt Project
     * @return List of job IDs
     * @throws IOException If fails
     */
    public Collection<String> jobs(final Project pkt) throws IOException {
        return this.item().xpath(
            String.format(
                "/agenda/order[project = '%s']/@job",
                pkt.pid()
            )
        );
    }

    /**
     * This job exists in the agenda?
     * @param job The job
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists(final String job) throws IOException {
        return this.item().read(xoc -> Agenda.exists(xoc, job));
    }

    /**
     * The job has inspector.
     * @param job Job id
     * @return TRUE if has
     * @throws IOException If fails
     */
    public boolean hasInspector(final String job) throws IOException {
        return !this.item().empty(
            String.format("%s/inspector", Agenda.path(job))
        );
    }

    /**
     * When the job was added to agenda?
     * @param job Job id
     * @return Date and time job was added to agenda
     * @throws IOException If fails
     */
    public Instant added(final String job) throws IOException {
        return Instant.parse(
            this.item().read(
                xoc -> this.ensureExists(xoc, job)
                    .nodes(
                        String.format(
                            "/agenda/order[@job='%s']/added",
                            job
                        )
                    ).get(0).node().getTextContent()
            )
        );
    }

    /**
     * Add an order to the agenda.
     * @param project The project
     * @param job Job ID
     * @param role The role
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void add(final Project project, final String job,
        final String role) throws IOException {
        this.item().update(
            new Directives()
                .xpath("/agenda")
                .add("order")
                .attr("job", job)
                .add("role").set(role).up()
                .add("title").set("-").up()
                .add("added").set(Instant.now(this.clock)).up()
                .add("project")
                .set(project.pid())
        );
    }

    /**
     * Remove an order from the agenda.
     * @param job The job to remove
     * @throws IOException If fails
     */
    public void remove(final String job) throws IOException {
        this.item().update(
            xoc -> this.ensureExists(xoc, job).modify(
                new Directives()
                    .xpath(Agenda.path(job))
                    .strict(1)
                    .remove()
            )
        );
    }

    /**
     * Remove all orders.
     * @throws IOException If fails.
     */
    public void removeAll() throws IOException {
        this.item().update(
            new Directives()
                .xpath("/agenda/order")
                .remove()
        );
    }

    /**
     * Add estimate.
     * @param job The job to mark
     * @param cash The estimate
     * @throws IOException If fails
     */
    public void estimate(final String job, final Cash cash) throws IOException {
        this.item().update(
            xoc -> this.ensureExists(xoc, job).modify(
                new Directives()
                    .xpath(Agenda.path(job))
                    .strict(1)
                    .addIf("estimate")
                    .set(cash)
            )
        );
    }

    /**
     * Add impediment.
     * @param job The job to mark
     * @param reason The reason
     * @throws IOException If fails
     */
    public void impediment(final String job,
        final String reason) throws IOException {
        this.item().update(
            xoc -> this.ensureExists(xoc, job).modify(
                new Directives()
                    .xpath(Agenda.path(job))
                    .strict(1)
                    .addIf("impediment")
                    .set(reason)
            )
        );
    }

    /**
     * Add inspector.
     * @param job Job id
     * @param inspector Inspector login
     * @throws IOException If fails
     */
    public void inspector(final String job,
        final String inspector) throws IOException {
        this.item().update(
            xoc -> this.ensureExists(xoc, job).modify(
                new Directives()
                    .xpath(Agenda.path(job))
                    .strict(1)
                    .addIf("inspector")
                    .set(inspector)
            )
        );
    }

    /**
     * Add title of the specified job.
     * @param job The job to modify
     * @param title The title to add
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void title(final String job, final String title) throws IOException {
        this.item().update(
            xoc -> this.ensureExists(xoc, job).modify(
                new Directives()
                    .xpath(Agenda.path(job))
                    .strict(1)
                    .addIf("title")
                    .set(title)
            )
        );
    }

    /**
     * Retrieves title of the specified job.
     * @param job The job to retrieve the text
     * @return The title of the job.
     * @throws IOException If fails
     */
    public String title(final String job) throws IOException {
        return this.item().read(
            xoc -> this.ensureExists(xoc, job).nodes(
                String.format(
                    "/agenda/order[@job='%s']/title",
                    job
                )
            )
        ).get(0).node().getTextContent();
    }

    /**
     * Retrieves the role of the specified job.
     * @param job The job whose role is to be retrieved.
     * @return The role of the job.
     * @throws IOException If fails.
     */
    public String role(final String job) throws IOException {
        return this.item().read(
            xoc -> this.ensureExists(xoc, job).nodes(
                String.format(
                    "/agenda/order[@job='%s']/role",
                    job
                )
            )
        ).get(0).node().getTextContent();
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private ItemXml item() throws IOException {
        return new ItemXml(
            this.pmo.acq(String.format("agenda/%s.xml", this.login)),
            "pmo/agenda"
        );
    }

    /**
     * Construct the pull path to the given job.
     * @param job The job
     * @return The full path to the given job
     */
    private static String path(final String job) {
        return String.format(
            "/agenda/order[@job='%s']",
            job
        );
    }

    /**
     * Check if job exists in xocument.
     * @param xoc Xocument
     * @param job Job
     * @return True if exists
     * @throws IOException On failure
     */
    private static boolean exists(final Xocument xoc, final String job)
        throws IOException {
        return !xoc.nodes(Agenda.path(job)).isEmpty();
    }

    /**
     * Ensure job exists in xocument.
     * @param xoc Xocument
     * @param job Job id
     * @return Same xocument
     * @throws IOException If doesn't exist
     */
    private Xocument ensureExists(final Xocument xoc, final String job)
        throws IOException {
        if (!Agenda.exists(xoc, job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not in the agenda of @%s, can't set title"
                ).say(job, this.login)
            );
        }
        return xoc;
    }
}
