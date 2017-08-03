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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.cactoos.iterable.ListOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
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
     * @return Itself
     * @throws IOException If fails
     */
    public Roles bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pm/staff/roles");
        }
        return this;
    }

    /**
     * Does it have any roles?
     * @return TRUE if it has some roles
     * @throws IOException If fails
     */
    public boolean isEmpty() throws IOException {
        try (final Item roles = this.item()) {
            return new Xocument(roles).nodes("/roles/person").isEmpty();
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
            final String login = person.toLowerCase(Locale.ENGLISH);
            new Xocument(roles.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/roles[not(person[@id='%s'])]",
                            login
                        )
                    )
                    .add("person")
                    .attr("id", login)
                    .xpath(
                        String.format(
                            "/roles/person[@id='%s']",
                            login
                        )
                    )
                    .strict(1)
                    .add("role")
                    .set(role)
            );
        }
    }

    /**
     * Resign all roles.
     * @param person The person
     * @throws IOException If fails
     */
    public void resign(final String person) throws IOException {
        try (final Item roles = this.item()) {
            final Xocument xoc = new Xocument(roles.path());
            xoc.modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/roles/person[ @id='%s']",
                            person
                        )
                    )
                    .strict(1)
                    .remove()
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
                    .strict(1)
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
     * Does he have any roles?
     * @param person The person
     * @return TRUE if it has any role
     * @throws IOException If fails
     */
    public boolean hasAnyRole(final String person) throws IOException {
        try (final Item roles = this.item()) {
            return new Xocument(roles).nodes(
                String.format(
                    "/roles/person[@id = '%s']",
                    person
                )
            ).iterator().hasNext();
        }
    }

    /**
     * Does he have any of these roles?
     * @param person The person
     * @param list Roles to find
     * @return TRUE if it has a role
     * @throws IOException If fails
     */
    public boolean hasRole(final String person, final String... list)
        throws IOException {
        try (final Item roles = this.item()) {
            return new Xocument(roles).nodes(
                String.format(
                    "/roles/person[@id='%s' and (%s)]",
                    person,
                    new JoinedText(
                        " or ",
                        new Mapped<>(
                            new ListOf<>(list),
                            role -> String.format("role='%s'", role)
                        )
                    ).asString()
                )
            ).iterator().hasNext();
        }
    }

    /**
     * Find GitHub logins by the given role.
     * @param role Role to find
     * @return List of GitHub logins
     * @throws IOException If fails
     */
    public List<String> findByRole(final String role) throws IOException {
        try (final Item roles = this.item()) {
            return new Xocument(roles).xpath(
                String.format(
                    "/roles/person[role='%s']/@id",
                    role
                )
            );
        }
    }

    /**
     * Find all roles of a given user.
     * @param login The login
     * @return List of user roles
     * @throws IOException If fails
     */
    public Collection<String> allRoles(final String login) throws IOException {
        try (final Item roles = this.item()) {
            return new Xocument(roles).xpath(
                String.format(
                    "/roles/person[@id='%s']/role/text()",
                    login
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
        return this.project.acq("roles.xml");
    }

}
