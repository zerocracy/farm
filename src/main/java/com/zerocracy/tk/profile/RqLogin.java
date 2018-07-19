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
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Projects;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import org.cactoos.Scalar;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Or;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.forward.RsForward;

/**
 * User login from the request.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RqLogin implements Scalar<String> {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * RqRegex.
     */
    private final RqRegex request;

    /**
     * Ctor.
     * @param frm The farm
     * @param req Request
     */
    public RqLogin(final Farm frm, final RqRegex req) {
        this.farm = frm;
        this.request = req;
    }

    @Override
    public String value() throws IOException {
        final String login = this.request.matcher()
            .group(1).toLowerCase(Locale.ENGLISH);
        final People people = new People(this.farm).bootstrap();
        if (!people.find("github", login).iterator().hasNext()) {
            throw new RsForward(
                new RsParFlash(
                    new Par("User @%s not found").say(login),
                    Level.SEVERE
                )
            );
        }
        final boolean owner = new IoCheckedScalar<>(
            new Or(
                new Mapped<>(
                    pid -> new IoCheckedScalar<>(
                        () -> new Roles(
                            this.farm.find(String.format("@id='%s'", pid))
                                .iterator().next()
                        ).bootstrap().hasRole(login, "PO")
                    ),
                    new Projects(this.farm, login).bootstrap().iterate()
                )
            )
        ).value();
        if (!new People(this.farm).bootstrap().hasMentor(login) && !owner) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "@%s is not invited to us yet, see §1"
                    ).say(login),
                    Level.WARNING
                )
            );
        }
        return login;
    }
}
