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
package com.zerocracy.pmo.recharge;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.pm.cost.Estimates;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Recharge of a project.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class Recharge {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Ctor.
     * @param frm Farm
     * @param project Project
     */
    public Recharge(final Farm frm, final Project project) {
        this.farm = frm;
        this.pkt = project;
    }

    /**
     * Recharge exists for the project?
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists() throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format(
                    "/catalog/project[@id='%s']/recharge", this.pkt.pid()
                )
            ).isEmpty();
        }
    }

    /**
     * Pay now.
     * @param claim The claim to fill out
     * @return New claim
     * @throws IOException If fails
     */
    public ClaimOut pay(final ClaimOut claim) throws IOException {
        if (!this.exists()) {
            throw new IllegalArgumentException(
                String.format(
                    "Recharge %s doesn't exist, can't pay", this.pkt.pid()
                )
            );
        }
        final XML xml;
        try (final Item item = this.item()) {
            xml = new Xocument(item.path()).nodes(
                String.format(
                    "/catalog/project[@id='%s']/recharge", this.pkt.pid()
                )
            ).get(0);
        }
        final String system = xml.xpath("system/text()").get(0);
        if (!"stripe".equals(system)) {
            throw new IllegalStateException(
                String.format(
                    "Unknown system \"%s\", can't recharge", system
                )
            );
        }
        final Cash amount = new Cash.S(xml.xpath("amount/text()").get(0));
        final String customer = xml.xpath("code/text()").get(0);
        final String tid = new Stripe(this.farm).charge(
            customer, amount,
            new Par("Project %s was recharged").say(this.pkt.pid())
        );
        return claim
            .type("Funded by Stripe")
            .param("amount", amount)
            .param("stripe_customer", customer)
            .param("payment_id", tid);
    }

    /**
     * Delete it.
     * @throws IOException If fails
     */
    public void delete() throws IOException {
        if (!this.exists()) {
            throw new IllegalArgumentException(
                String.format(
                    "Recharge %s doesn't exist, can't delete", this.pkt.pid()
                )
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/catalog/project[@id='%s']/recharge", this.pkt.pid()
                    )
                ).strict(1).remove()
            );
        }
    }

    /**
     * How much will be recharged.
     * @return Amount to recharge
     * @throws IOException If fails
     */
    public Cash amount() throws IOException {
        if (!this.exists()) {
            throw new IllegalArgumentException(
                String.format(
                    "Recharge %s doesn't exist, can't read amount",
                    this.pkt.pid()
                )
            );
        }
        try (final Item item = this.item()) {
            return new Cash.S(
                new Xocument(item.path()).xpath(
                    String.format(
                        "/catalog/project[@id='%s']/recharge/amount/text()",
                        this.pkt.pid()
                    )
                ).get(0)
            );
        }
    }

    /**
     * Does recharge required?
     * @return TRUE if required
     * @throws IOException If fails
     */
    public boolean required() throws IOException {
        final Cash cash = new Ledger(this.pkt).bootstrap().cash();
        final Cash locked = new Estimates(this.pkt).bootstrap().total();
        return cash.compareTo(locked.add(new Cash.S("$16"))) < 0;
    }

    /**
     * Create a recharge.
     * @param system The system
     * @param amount The amount
     * @param code The code
     * @throws IOException If fails
     */
    public void set(final String system, final Cash amount,
        final String code) throws IOException {
        if (!new Catalog(this.farm).bootstrap().exists(this.pkt.pid())) {
            throw new IllegalArgumentException(
                String.format(
                    "Project %s doesn't exist in catalog, can't set",
                    this.pkt.pid()
                )
            );
        }
        if (this.exists()) {
            this.delete();
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/catalog/project[@id='%s']", this.pkt.pid()
                        )
                    )
                    .add("recharge")
                    .add("system").set(system).up()
                    .add("amount").set(amount).up()
                    .add("code").set(code).up()
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return new Pmo(this.farm).acq("catalog.xml");
    }
}
