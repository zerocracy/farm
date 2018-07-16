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
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.cactoos.Scalar;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.forward.RsForward;

/**
 * User login from the request.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RqSecureLogin implements Scalar<String> {

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
    RqSecureLogin(final Farm frm, final RqRegex req) {
        this.farm = frm;
        this.request = req;
    }

    @Override
    public String value() throws IOException {
        final String login = new RqLogin(this.farm, this.request).value();
        final String user = new RqUser(this.farm, this.request).value();
        if (!user.equals(login)
            && !new Roles(new Pmo(this.farm)).bootstrap().hasAnyRole(user)) {
            throw new RsForward(
                new RsParFlash(
                    new Par(
                        "Only @%s is allowed to see this page"
                    ).say(login),
                    Level.SEVERE
                )
            );
        }
        return login;
    }
}
