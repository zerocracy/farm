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
import com.zerocracy.cash.Cash;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pmo.Rfps;
import com.zerocracy.pmo.recharge.Stripe;
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
 * Pay for RFP.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkPrepay implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkPrepay(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final String user = new RqUser(this.farm, req, false).value();
        final Rfps rfps = new Rfps(this.farm).bootstrap();
        if (rfps.exists(user)) {
            throw new RsForward(
                new RsParFlash(
                    new Par("You have already paid").say(),
                    Level.WARNING
                ),
                "/rfp"
            );
        }
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String email = form.single("email");
        final Cash amount = new Cash.S(
            String.format(
                "$%.2f", Double.parseDouble(form.single("cents")) / 100.0d
            )
        );
        final String customer;
        try {
            customer = new Stripe(this.farm).pay(
                form.single("token"), email, amount, "RFP"
            );
        } catch (final Stripe.PaymentException ex) {
            throw new RsForward(new RsParFlash(ex), "/rfp");
        }
        final int rid = rfps.pay(
            user, String.format("Stripe ID: %s", customer), email
        );
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                "RFP #%d has been paid by @%s: %s;",
                "we will notify you when they submit the statement of work"
            ).say(rid, user, email)
        ).postTo(new ClaimsOf(this.farm));
        return new RsForward(
            new RsParFlash(
                new Par("The RFP #%d has been paid, thanks").say(rid),
                Level.INFO
            ),
            "/rfp"
        );
    }

}
