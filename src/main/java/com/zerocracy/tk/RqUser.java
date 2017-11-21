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
package com.zerocracy.tk;

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;
import org.takes.Request;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;

/**
 * User login from OAuth.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
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
        this(new Pmo(farm), req);
    }

    /**
     * Ctor.
     * @param pmo The PMO
     * @param req Request
     */
    public RqUser(final Project pmo, final Request req) {
        this.user = new SolidScalar<>(
            () -> {
                final Identity identity = new RqAuth(req).identity();
                if (identity.equals(Identity.ANONYMOUS)) {
                    throw new RsForward(
                        new RsFlash(
                            "You must be logged in.",
                            Level.WARNING
                        )
                    );
                }
                final String login = identity.properties()
                    .get("login").toLowerCase(Locale.ENGLISH);
                final People people = new People(pmo).bootstrap();
                if (!people.hasMentor(login)) {
                    throw new RsForward(
                        new RsFlash(
                            String.join(
                                " ",
                                // @checkstyle LineLength (5 lines)
                                String.format("You \"@%s\" must be invited", login),
                                "to us by someone we already know.",
                                "If you don't know anyone who works with us already,",
                                "email us to join@zerocracy.com and we'll see what",
                                "we can do."
                            ),
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
