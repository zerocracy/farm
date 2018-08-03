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
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqHref;

/**
 * Add user alias.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkAlias implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkAlias(final Farm frm) {
        this.farm = frm;
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Response act(final Request req) throws IOException {
        final RqHref.Smart smart = new RqHref.Smart(new RqHref.Base(req));
        final String rel = smart.single("rel");
        final String href = smart.single("href");
        final Pmo pmo = new Pmo(this.farm);
        final People people = new People(pmo).bootstrap();
        if (people.find(rel, href).iterator().hasNext()) {
            throw new RsForward(
                new RsParFlash(
                    "We've been already introduced, thanks!",
                    Level.WARNING
                )
            );
        }
        final String login = new RqUser(this.farm, req, false).value();
        people.link(login, rel, href);
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "We just linked @%s via %s as \"%s\""
            ).say(login, rel, href)
        ).postTo(new ClaimsOf(this.farm));
        return new RsForward(
            new RsParFlash(
                new Par(
                    "Thanks, @%s now has an alias, @rel='%s', @href='%s'"
                ).say(login, rel, href),
                Level.INFO
            )
        );
    }
}
