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

import com.zerocracy.ItemXml;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.SolidList;
import org.cactoos.text.JoinedText;
import org.xembly.Directives;

/**
 * Project roles.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals",
        "PMD.NPathComplexity",
        "PMD.CyclomaticComplexity"
    }
)
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
     */
    public Roles bootstrap() {
        return this;
    }

    /**
     * Does it have any roles?
     * @return TRUE if it has some roles
     * @throws IOException If fails
     */
    public boolean isEmpty() throws IOException {
        return this.item().empty("/roles/person");
    }

    /**
     * Everybody.
     * @return GitHub logins of everybody
     * @throws IOException If fails
     */
    public Collection<String> everybody() throws IOException {
        return this.item().xpath("/roles/person/@id");
    }

    /**
     * Assign role.
     * @param person The person
     * @param role The role to assign
     * @throws IOException If fails
     * @checkstyle CyclomaticComplexityCheck (100 lines)
     */
    public void assign(final String person, final String role)
        throws IOException {
        if (!role.matches("DEV|REV|QA|PO|TST|ARC")) {
            throw new SoftException(
                new Par(
                    "The role %s is not one of those we recognize.",
                    "Try to use DEV, REV, ARC, QA, PO, or TST."
                ).say(role)
            );
        }
        if ("QA".equals(role) && this.hasRole(person, "DEV", "REV", "ARC")) {
            throw new SoftException(
                new Par(
                    "The user @%s already has a technical role in the project",
                    "can't be a QA at the same time, see §34"
                ).say(person)
            );
        }
        if ("REV".equals(role) && this.hasRole(person, "ARC")) {
            throw new SoftException(
                new Par(
                    "The architect @%s can't be a reviewer",
                    "at the same time, see §34"
                ).say(person)
            );
        }
        if ("ARC".equals(role) && this.hasRole(person, "REV")) {
            throw new SoftException(
                new Par(
                    "The reviewer @%s can't be",
                    "an architect at the same time, see §34"
                ).say(person)
            );
        }
        if ("ARC".equals(role) && this.findByRole("ARC").size() > 1) {
            throw new SoftException(
                // @checkstyle LineLength (1 line)
                new Par("A project can't have more than two ARCs, see §34: @%s").say(
                    new JoinedText(", @", this.findByRole("ARC"))
                )
            );
        }
        if ("PO".equals(role) && this.findByRole("PO").size() > 1) {
            throw new SoftException(
                // @checkstyle LineLength (1 line)
                new Par("A project can't have more than two POs, see §34: @%s").say(
                    new JoinedText(", @", this.findByRole("PO"))
                )
            );
        }
        final String login = person.toLowerCase(Locale.ENGLISH);
        this.item().update(
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

    /**
     * Resign role.
     * @param person The person
     * @param role The role to resign
     * @throws IOException If fails
     */
    public void resign(final String person, final String role)
        throws IOException {
        if (!this.hasRole(person, role)) {
            throw new SoftException(
                new Par(
                    "@%s doesn't have %s role in the project"
                ).say(person, role)
            );
        }
        if ("PO".equals(role) && this.findByRole(role).size() < 2) {
            throw new SoftException(
                new Par(
                    "You can't remove all",
                    "product owners from the project, see §34"
                ).say()
            );
        }
        if ("ARC".equals(role) && this.findByRole(role).size() < 2) {
            throw new SoftException(
                new Par(
                    "You can't remove all",
                    "architects from the project, see §34"
                ).say()
            );
        }
        this.item().update(
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

    /**
     * Does he have any roles?
     * @param person The person
     * @return TRUE if it has any role
     * @throws IOException If fails
     */
    public boolean hasAnyRole(final String person) throws IOException {
        return this.item().exists(
            String.format(
                "/roles/person[@id = '%s']",
                person
            )
        );
    }

    /**
     * Does he have any of these roles?
     * @param person The person
     * @param arr Roles to find
     * @return TRUE if it has a role
     * @throws IOException If fails
     */
    public boolean hasRole(final String person, final String... arr)
        throws IOException {
        final List<String> list = Arrays.asList(arr);
        if (list.isEmpty()) {
            throw new IllegalArgumentException(
                "The list of roles can't be empty, use hasAnyRoles() instead"
            );
        }
        return this.item().exists(
            String.format(
                "/roles/person[@id='%s' and (%s)]",
                person,
                new JoinedText(
                    " or ",
                    new Mapped<>(
                        role -> String.format("role='%s'", role),
                        new SolidList<>(list)
                    )
                ).asString()
            )
        );
    }

    /**
     * Find GitHub logins by the given role.
     * @param role Role to find
     * @return List of GitHub logins
     * @throws IOException If fails
     */
    public List<String> findByRole(final String role) throws IOException {
        return this.item().xpath(
            String.format(
                "/roles/person[role='%s']/@id",
                role
            )
        );
    }

    /**
     * Find all roles of a given user.
     * @param login The login
     * @return List of user roles
     * @throws IOException If fails
     */
    public Collection<String> allRoles(final String login) throws IOException {
        return this.item().xpath(
            String.format(
                "/roles/person[@id='%s']/role/text()",
                login
            )
        );
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private ItemXml item() throws IOException {
        return new ItemXml(this.project.acq("roles.xml"), "pm/staff/roles");
    }
}
