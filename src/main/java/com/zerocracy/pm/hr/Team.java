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

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.Person;
import com.zerocracy.pm.Xocument;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Project team.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Team {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Team(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @throws IOException If fails
     */
    public void bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("team");
        }
    }

    /**
     * Does he have any of these roles?
     * @param person The person
     * @param roles Roles to find
     * @return TRUE if it has a role
     * @throws IOException If fails
     */
    public boolean hasRole(final Person person, final String... roles)
        throws IOException {
        return this.hasRole(person, Arrays.asList(roles));
    }

    /**
     * Does he have any of these roles?
     * @param person The person
     * @param roles Roles to find
     * @return TRUE if it has a role
     * @throws IOException If fails
     */
    public boolean hasRole(final Person person, final Iterable<String> roles)
        throws IOException {
        try (final Item team = this.item()) {
            return new Xocument(team).xpath(
                String.format(
                    "/team/member[name='%s' and (%s)]",
                    person.name(),
                    String.join(
                        " or ",
                        StreamSupport.stream(roles.spliterator(), false)
                            .map(role -> String.format("roles/role=%s", role))
                            .collect(Collectors.toList())
                    )
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
        return this.project.acq("team.xml");
    }

}
