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
package com.zerocracy.tk.project;

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.RqUser;
import java.io.IOException;
import java.util.logging.Level;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.forward.RsFailure;
import org.takes.facets.forward.RsForward;

/**
 * Project from the request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RqProject implements Project {

    /**
     * Project.
     */
    private final Scalar<Project> pkt;

    /**
     * Ctor.
     * @param farm Farm
     * @param req Request
     * @throws IOException If fails
     */
    RqProject(final Farm farm, final RqRegex req) throws IOException {
        this.pkt = new SyncScalar<>(
            new StickyScalar<>(
                () -> {
                    final String name = req.matcher().group(1);
                    final Project pmo = new Pmo(farm);
                    final Catalog catalog = new Catalog(pmo).bootstrap();
                    if (!catalog.exists(name)) {
                        throw new RsFailure(
                            String.format("Project \"%s\" not found", name)
                        );
                    }
                    final Project project = farm.find(
                        String.format("@id='%s'", name)
                    ).iterator().next();
                    final String login = new RqUser(farm, req).value();
                    if (!"yegor256".equals(login)
                        && !new Roles(project).bootstrap()
                        .hasRole(login, "ARC", "PO")) {
                        throw new RsForward(
                            new RsFlash(
                                String.format(
                                    "@%s must either be PO or ARC to view %s",
                                    login, name
                                ),
                                Level.SEVERE
                            )
                        );
                    }
                    return project;
                }
            )
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
