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
package com.zerocracy.pm.staff;

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.xembly.Directives;

/**
 * People banned from the job.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.13
 */
public final class Bans {

    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param project A project
     */
    public Bans(final Project project) {
        this.project = project;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Bans bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pm/staff/bans");
        }
        return this;
    }

    /**
     * Check either user banned from this job or not.
     * @param job A job
     * @param login User to check
     * @return Reasons of bans
     * @throws IOException If fails
     */
    public List<String> reasons(final String job, final String login)
        throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format(
                    "/bans/ban[@job='%s' and login/text()='%s']/reason/text()",
                    job,
                    login
                )
            );
        }
    }

    /**
     * Add ban.
     * @param job Banned job
     * @param login User to ban
     * @param reason Ban reason
     * @throws IOException If fails
     */
    public void ban(
        final String job,
        final String login,
        final String reason
    ) throws IOException {
        final String date = ZonedDateTime.now().format(
            DateTimeFormatter.ISO_INSTANT
        );
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath("/bans")
                    .add("ban")
                    .attr("job", job)
                    .add("created").set(date).up()
                    .add("login").set(login).up()
                    .add("reason").set(reason).up()
                    .up()
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("bans.xml");
    }
}
