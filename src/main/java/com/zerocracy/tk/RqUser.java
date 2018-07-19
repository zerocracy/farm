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
package com.zerocracy.tk;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Projects;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.logging.Level;
import org.cactoos.Scalar;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Or;
import org.cactoos.scalar.SolidScalar;
import org.takes.Request;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.forward.RsForward;
import org.takes.facets.previous.RsPrevious;
import org.takes.rq.RqRequestLine;

/**
 * User login from OAuth.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RqUser implements Scalar<String> {

    /**
     * Project.
     */
    private final Scalar<String> user;

    /**
     * Ctor.
     * @param farm Farm
     * @param req Request
     */
    public RqUser(final Farm farm, final Request req) {
        this(farm, req, true);
    }

    /**
     * Ctor.
     * @param farm Farm
     * @param req Request
     * @param invited TRUE if we need a user to be invited already
     * @todo #1054:30min A check whether user is a PO in any of the projects
     *  happens in this method and in RqLogin#value. Extract this check to
     *  a common place and reuse it in both places.
     */
    public RqUser(final Farm farm, final Request req,
        final boolean invited) {
        this.user = new SolidScalar<>(
            () -> {
                final Identity identity = new RqAuth(req).identity();
                if (identity.equals(Identity.ANONYMOUS)) {
                    throw new RsForward(
                        new RsPrevious(
                            new RsParFlash(
                                String.format(
                                    "You must be logged in to see %s",
                                    new URI(
                                        new RqRequestLine.Base(req).uri()
                                    ).getPath()
                                ),
                                Level.WARNING
                            ),
                            new RqRequestLine.Base(req).uri()
                        )
                    );
                }
                final String login = identity.properties()
                    .get("login").toLowerCase(Locale.ENGLISH);
                final boolean owner = new IoCheckedScalar<>(
                    new Or(
                        new Mapped<>(
                            pid -> new IoCheckedScalar<>(
                                () -> new Roles(
                                    farm.find(
                                        String.format("@id='%s'", pid)
                                    ).iterator().next()
                                ).bootstrap().hasRole(login, "PO")
                            ),
                            new Projects(farm, login).bootstrap().iterate()
                        )
                    )
                ).value();
                if (invited && !new People(farm).bootstrap().hasMentor(login)
                    && !owner) {
                    throw new RsForward(
                        new RsParFlash(
                            new Par(
                                "You (@%s) must be invited",
                                "to us by someone we already know, see §1"
                            ).say(login),
                            Level.WARNING
                        )
                    );
                }
                return login;
            }
        );
    }

    @Override
    public String value() throws IOException {
        return new IoCheckedScalar<>(this.user).value();
    }
}
