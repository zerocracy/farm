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
package com.zerocracy.pmo;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.util.Collection;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Agenda of one person in one Project, tasks that
 * are assigned to them in that Project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @todo #422:30min Title element, of an order in agenda, has been declared in
 *  agenda.xsd. Let's add method title(...), which will set the title of
 *  on order and add a new stakeholder, set_agenda_title_from_github.groovy,
 *  which will get title of a job from GitHub and set it to Agenda. THe reason
 *  for the new stakeholder is that not all orders will come from Github, some
 *  may also come from Jira, Trello etc, in the future.
 */
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
     * Ctor.
     * @param farm The farm
     * @param user The user
     */
    public Agenda(final Farm farm, final String user) {
        this(new Pmo(farm), user);
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param user The user
     */
    public Agenda(final Project pkt, final String user) {
        this.pmo = pkt;
        this.login = user;
    }

    /**
     * Bootstrap it.
     * @return This
     * @throws IOException If fails
     */
    public Agenda bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/agenda");
        }
        return this;
    }

    /**
     * Return full list of jobs this person currently is working with.
     * @return List of job IDs
     * @throws IOException If fails
     */
    public Collection<String> jobs() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                "/agenda/order/@job"
            );
        }
    }

    /**
     * This job exists in the agenda?
     * @param job The job
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists(final String job) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format("/agenda/order[@job= '%s']", job)
            ).isEmpty();
        }
    }

    /**
     * Add an order to the agenda.
     * @param project The project
     * @param job Job ID
     * @param role The role
     * @throws IOException If fails
     */
    public void add(final Project project, final String job,
        final String role) throws IOException {
        if (this.exists(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is already in the agenda of @%s"
                ).say(job, this.login)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/agenda")
                    .add("order")
                    .attr("job", job)
                    .add("role").set(role).up()
                    .add("title").set("-").up()
                    .add("added").set(new DateAsText().asString()).up()
                    .add("project")
                    .set(project.pid())
            );
        }
    }

    /**
     * Remove an order from the agenda.
     * @param job The job to remove
     * @throws IOException If fails
     */
    public void remove(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not in the agenda of @%s, can't remove"
                ).say(job, this.login)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/agenda/order[@job='%s']", job))
                    .strict(1)
                    .remove()
            );
        }
    }

    /**
     * Remove all orders.
     * @throws IOException If fails.
     */
    public void removeAll() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/agenda/order")
                    .remove()
            );
        }
    }

    /**
     * Add estimate.
     * @param job The job to mark
     * @param cash The estimate
     * @throws IOException If fails
     */
    public void estimate(final String job, final Cash cash) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not in the agenda of @%s, can't set estimate"
                ).say(job, this.login)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/agenda/order[@job='%s' ]", job))
                    .strict(1)
                    .addIf("estimate")
                    .set(cash)
            );
        }
    }

    /**
     * Add estimate.
     * @param job The job to mark
     * @param reason The reason
     * @throws IOException If fails
     */
    public void impediment(final String job,
        final String reason) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not in the agenda of @%s, can't set impediment"
                ).say(job, this.login)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/agenda/order[@job= '%s' ]", job))
                    .strict(1)
                    .addIf("impediment")
                    .set(reason)
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq(
            String.format("agenda/%s.xml", this.login)
        );
    }

}
