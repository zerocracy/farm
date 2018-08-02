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
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.recharge.Stripe;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;

/**
 * Pay page.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkStripePay implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkStripePay(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Project project = new RqProject(this.farm, req, "PO");
        final RqFormSmart form = new RqFormSmart(new RqGreedy(req));
        final String email = form.single("email");
        final Cash amount = new Cash.S(
            String.format(
                "USD %.2f",
                // @checkstyle MagicNumber (1 line)
                Double.parseDouble(form.single("cents")) / 100.0d
            )
        );
        final Stripe stripe = new Stripe(this.farm);
        final String customer;
        final String pid;
        try {
            customer = stripe.register(
                form.single("token"), email
            );
            pid = stripe.charge(
                customer, amount,
                new Par(this.farm, "Project %s funded").say(project.pid())
            );
        } catch (final Stripe.PaymentException ex) {
            throw new RsForward(
                new RsParFlash(ex),
                String.format("/p/%s", project.pid())
            );
        }
        final String user = new RqUser(this.farm, req, false).value();
        new ClaimOut()
            .type("Funded by Stripe")
            .param("amount", amount)
            .param("stripe_customer", customer)
            .param("payment_id", pid)
            .param("email", email)
            .author(user)
            .postTo(new ClaimsOf(this.farm, project));
        new ClaimOut().type("Notify PMO").param(
            "message", new Par(
                this.farm,
                "Project %s was funded for %s by @%s;",
                "customer ID is `%s`, payment ID is `%s`"
            ).say(project.pid(), amount, user, customer, pid)
        ).postTo(new ClaimsOf(this.farm, new Pmo(this.farm)));
        return new RsForward(
            new RsParFlash(
                new Par(
                    this.farm,
                    "The project %s was successfully funded for %s;",
                    "the ledger will be updated in a few minutes;",
                    "payment ID is `%s`"
                ).say(project.pid(), amount, pid),
                Level.INFO
            ),
            String.format("/p/%s", project.pid())
        );
    }

}
