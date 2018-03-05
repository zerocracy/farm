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
package com.zerocracy.pm.time;

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.util.Collection;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Order reminders.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.18
 */
public final class Reminders {

    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param project Project
     */
    public Reminders(final Project project) {
        this.project = project;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Reminders bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pm/time/reminders");
        }
        return this;
    }

    /**
     * Add job to remind.
     * @param job Job id.
     * @param login Order's login
     * @param label Label to show
     * @return TRUE if actually added
     * @throws IOException If fails
     */
    public boolean add(final String job, final String login, final String label)
        throws IOException {
        try (final Item item = this.item()) {
            final Xocument xoc = new Xocument(item);
            final String xpath = String.format(
                "/reminders/order[@job = '%s']",
                job
            );
            if (xoc.nodes(xpath).isEmpty()) {
                xoc.modify(
                    new Directives().xpath("/reminders")
                        .add("order").attr("job", job)
                );
            }
            boolean added = false;
            if (xoc.nodes(
                String.format(
                    "%s/reminder/label[text() = '%s']",
                    xpath, label
                )
            ).isEmpty()) {
                xoc.modify(
                    new Directives()
                        .xpath(xpath)
                        .strict(1)
                        .add("reminder")
                        .add("created").set(new DateAsText().asString()).up()
                        .add("label")
                        .set(label)
                        .up()
                        .add("login")
                        .set(login)
                        .up()
                );
                added = true;
            }
            return added;
        }
    }

    /**
     * Reminder labels for job.
     * @param job Job id
     * @return Label list
     * @throws IOException If fails
     */
    public Collection<String> labels(final String job) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .xpath(
                    String.format(
                        "/reminders/order[@job = '%s']/reminder/label/text()",
                        job
                    )
                );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("reminders.xml");
    }
}
