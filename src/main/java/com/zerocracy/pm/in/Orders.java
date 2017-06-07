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
package com.zerocracy.pm.in;

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.xembly.Directives;

/**
 * Work orders.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Orders {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Orders(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Orders bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/in/orders");
        }
        return this;
    }

    /**
     * Assign job to performer.
     * @param job The job to assign
     * @param login The login of the user
     * @throws IOException If fails
     */
    public void assign(final String job, final String login)
        throws IOException {
        if (this.assigned(job)) {
            throw new SoftException(
                String.format(
                    "Job `%s` already assigned to @%s",
                    job, login
                )
            );
        }
        try (final Item wbs = this.item()) {
            final Xocument xocument = new Xocument(wbs.path());
            xocument.modify(
                new Directives()
                    .xpath(String.format("/orders[not(order[@job='%s'])]", job))
                    .strict(1)
                    .add("order")
                    .attr("job", job)
                    .add("created")
                    .set(
                        ZonedDateTime.now().format(
                            DateTimeFormatter.ISO_INSTANT
                        )
                    )
                    .up()
                    .add("performer")
                    .set(login)
            );
        }
    }

    /**
     * Resign current job performer.
     * @param job The job to touch
     * @throws IOException If fails
     */
    public void resign(final String job) throws IOException {
        if (!this.assigned(job)) {
            throw new SoftException(
                String.format(
                    "Job `%s` is not assigned to anyone",
                    job
                )
            );
        }
        try (final Item wbs = this.item()) {
            final Xocument xocument = new Xocument(wbs.path());
            xocument.modify(
                new Directives()
                    .xpath(String.format("/orders/order[@job ='%s']", job))
                    .strict(1)
                    .remove()
            );
        }
    }

    /**
     * Job is assigned.
     * @param job The job
     * @return TRUE if assigned
     * @throws IOException If fails of it there is no assignee
     */
    public boolean assigned(final String job) throws IOException {
        try (final Item wbs = this.item()) {
            return !new Xocument(wbs.path()).nodes(
                String.format("/orders/order[@job='%s']", job)
            ).isEmpty();
        }
    }

    /**
     * Get job assignee.
     * @param job The job
     * @return Performer GitHub login
     * @throws IOException If fails of it there is no assignee
     */
    public String performer(final String job) throws IOException {
        if (!this.assigned(job)) {
            throw new SoftException(
                String.format(
                    "Job `%s` is not assigned, can't asValue performer", job
                )
            );
        }
        try (final Item wbs = this.item()) {
            return new Xocument(wbs.path()).xpath(
                String.format("/orders/order[@job='%s']/performer/text()", job)
            ).get(0);
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("orders.xml");
    }

}
