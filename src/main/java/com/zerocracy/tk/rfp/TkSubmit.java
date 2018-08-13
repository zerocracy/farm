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
package com.zerocracy.tk.rfp;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pmo.Rfps;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;

/**
 * Submit RFP.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkSubmit implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkSubmit(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String user = new RqUser(this.farm, req, false).value();
        final Rfps rfps = new Rfps(this.farm).bootstrap();
        if (!rfps.exists(user)) {
            throw new RsForward(
                new RsParFlash(
                    new Par("You have to pay first, see §41").say(),
                    Level.WARNING
                ),
                "/rfp"
            );
        }
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String sow = form.single("sow");
        final boolean complete = rfps.complete(user);
        final int rid = rfps.post(user, sow);
        if (!complete) {
            new ClaimOut()
                .type("Notify all")
                .param(
                    "message",
                    new Par(
                        "New RFP #%d was published,",
                        "you can [join](/rfps) as an architect;",
                        "a potential client needs an architect and the system",
                        "selected you, as one of the best developers",
                        "we have in house;",
                        "you can 'buy' this RFP",
                        "and get in touch with the client,",
                        "see §40"
                    ).say(rid)
                )
                .param("min", new Policy().get("40.min", 0))
                .param("reason", "RFP")
                .postTo(new ClaimsOf(this.farm));
        }
        return new RsForward(
            new RsParFlash(
                new Par("The statement of work has been updated, thanks").say(),
                Level.INFO
            ),
            "/rfp"
        );
    }

}
