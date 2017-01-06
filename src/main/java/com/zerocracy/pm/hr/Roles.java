/**
 * Copyright (c) 2016 Zerocracy
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
package com.zerocracy.pm.hr;

import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Project roles.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Roles {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Roles(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @throws IOException If fails
     */
    public void bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("roles", "pm/hr/roles");
        }
    }

    /**
     * Print it to text.
     * @return Text
     * @throws IOException If fails
     */
    public String print() throws IOException {
        try (final Item roles = this.item()) {
            return new XSLDocument(
                Roles.class.getResource("roles/to-text.xsl")
            ).applyTo(new XMLDocument(roles.path().toFile()));
        }
    }

    /**
     * Assign role.
     * @param person The person
     * @param role The role to assign
     * @throws IOException If fails
     */
    public void assign(final String person, final String role)
        throws IOException {
        try (final Item roles = this.item()) {
            new Xocument(roles.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/roles[not(person[@id='%s'])]",
                            person
                        )
                    )
                    .add("person")
                    .attr("id", person)
                    .xpath(
                        String.format(
                            "/roles/person[@id='%s']",
                            person
                        )
                    )
                    .add("role")
                    .set(role)
            );
        }
    }

    /**
     * Resign role.
     * @param person The person
     * @param role The role to resign
     * @throws IOException If fails
     */
    public void resign(final String person, final String role)
        throws IOException {
        try (final Item roles = this.item()) {
            final Xocument xoc = new Xocument(roles.path());
            xoc.modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/roles/person[@id='%s']/role[.='%s']",
                            person, role
                        )
                    )
                    .remove()
                    .xpath(
                        String.format(
                            "/roles/person[@id='%s' and not(role)]",
                            person
                        )
                    )
                    .remove()
            );
        }
    }

    /**
     * Does he have any of these roles?
     * @param person The person
     * @param role Roles to find
     * @return TRUE if it has a role
     * @throws IOException If fails
     */
    public boolean hasRole(final String person, final String role)
        throws IOException {
        try (final Item roles = this.item()) {
            return new Xocument(roles).xpath(
                String.format(
                    "/roles/person[@id='%s' and role='%s']/text()",
                    person, role
                )
            ).iterator().hasNext();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("roles.xml");
    }

}
