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
package com.zerocracy.pm.staff;

import com.jcabi.xml.XML;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.time.Instant;
import org.xembly.Directives;

/**
 * User applications to the project.
 *
 * @since 1.0
 * @todo #1349:30min Remove application when user get the role in project
 *  (invited by ARC or PO), keep in mind, that user can be invited even he/she
 *  is not in the `applications.xml` list. Also, let's remove old application
 *  (older than one month), we can check it periodically every week.
 */
public final class Applications {

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Applications(final Project pkt) {
        this.pkt = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Applications bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pm/staff/applications");
        }
        return this;
    }

    /**
     * Submit new application, override existing.
     *
     * @param login User login
     * @param role Role
     * @param rate Requested rate
     * @throws IOException If fails
     */
    public void submit(final String login, final String role,
        final Cash rate) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath("/applications")
                    .push()
                    .xpath(String.format("application[@login='%s']", login))
                    .remove()
                    .pop()
                    .add("application")
                    .attr("login", login)
                    .add("created").set(Instant.now().toString()).up()
                    .add("role").set(role).up()
                    .add("rate").set(rate).up()
                    .up()
            );
        }
    }

    /**
     * All applications nodes.
     * @return XML nodes
     * @throws IOException If fails
     */
    public Iterable<XML> all() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).nodes("/applications/application");
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pkt.acq("applications.xml");
    }
}
