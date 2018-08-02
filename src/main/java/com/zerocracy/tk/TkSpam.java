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
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;

/**
 * Send spam to all users.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkSpam implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkSpam(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String user = new RqUser(this.farm, req).value();
        if (!new Roles(new Pmo(this.farm)).bootstrap().hasAnyRole(user)) {
            throw new RsForward(
                new RsParFlash(
                    "You are not allowed to see this page, sorry",
                    Level.WARNING
                )
            );
        }
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String body = form.single("body");
        new ClaimOut().type("Notify all").param(
            "message", new Par(body).say()
        ).postTo(new ClaimsOf(this.farm));
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "Spam request has been submitted by @%s"
            ).say(user)
        ).postTo(new ClaimsOf(this.farm));
        return new RsForward(
            new RsParFlash(
                new Par(
                    "The message has been sent to all our users"
                ).say(),
                Level.INFO
            ),
            "/spam"
        );
    }

}
