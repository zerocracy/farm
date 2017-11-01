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
package com.zerocracy.pmo;

import com.jcabi.log.Logger;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.xembly.Directives;

/**
 * Agenda of one person.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
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
    public Iterable<String> jobs() throws IOException {
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
     * @param job Job ID
     * @param href HREF of the ticket
     * @throws IOException If fails
     */
    public void add(final String job, final String href)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/agenda")
                    .add("order")
                    .attr("job", job)
                    .add("href")
                    .set(href)
                    .up()
                    .add("added")
                    .set(
                        ZonedDateTime.now().format(
                            DateTimeFormatter.ISO_INSTANT
                        )
                    )
                    .up()
                    .add("project")
                    .set(this.pmo.pid())
            );
        }
        Logger.info(
            this, "New agenda item added job=\"%s\", href=\"%s\"",
            job, href
        );
    }

    /**
     * Remove an order from the agenda.
     * @param job The job to remove
     * @throws IOException If fails
     */
    public void remove(final String job) throws IOException {
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
