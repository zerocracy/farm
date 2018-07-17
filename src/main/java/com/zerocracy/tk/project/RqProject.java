/*
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
package com.zerocracy.tk.project;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.cactoos.Scalar;
import org.cactoos.iterable.IterableOf;
import org.cactoos.iterable.LengthOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.forward.RsForward;

/**
 * Project from the request.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class RqProject implements Project {

    /**
     * Project.
     */
    private final Scalar<Project> pkt;

    /**
     * Ctor.
     * @param farm Farm
     * @param req Request
     * @param required Roles required to get access
     */
    public RqProject(final Farm farm, final RqRegex req,
        final String... required) {
        this.pkt = new SolidScalar<>(
            () -> {
                final Project pmo = new Pmo(farm);
                final Project project = new RqAnonProject(farm, req);
                final String user = new RqUser(farm, req, false).value();
                final Roles roles = new Roles(project).bootstrap();
                final Roles admins = new Roles(pmo).bootstrap();
                if (new LengthOf(new IterableOf<>(required)).intValue() > 0
                    && !roles.hasRole(user, required)
                    && !admins.hasAnyRole(user)) {
                    throw new RsForward(
                        new RsParFlash(
                            new Par(
                                "You don't have any of these roles",
                                "in %s to view the page: %s"
                            ).say(project.pid(), String.join(", ", required)),
                            Level.WARNING
                        )
                    );
                }
                if (new LengthOf(new IterableOf<>(required)).intValue() == 0
                    && !roles.hasAnyRole(user)
                    && !admins.hasAnyRole(user)
                    && !new Catalog(farm).published(project.pid())) {
                    throw new RsForward(
                        new RsParFlash(
                            new Par(
                                "You are not a member of %s",
                                "and the project is not public"
                            ).say(project.pid()),
                            Level.WARNING
                        )
                    );
                }
                final People people = new People(farm).bootstrap();
                if (!roles.hasRole(user, "PO") && !people.hasMentor(user)) {
                    throw new RsForward(
                        new RsParFlash(
                            new Par(
                                "You are not a PO of %s",
                                "and you don't have a mentor yet, see §1"
                            ).say(project.pid()),
                            Level.WARNING
                        )
                    );
                }
                return project;
            }
        );
    }

    @Override
    public String pid() throws IOException {
        return new IoCheckedScalar<>(this.pkt).value().pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        return new IoCheckedScalar<>(this.pkt).value().acq(file);
    }

}
