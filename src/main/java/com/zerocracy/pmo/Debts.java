/**
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
package com.zerocracy.pmo;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.cactoos.time.DateAsText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Debts.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.21
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Debts {

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Debts(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public Debts(final Project pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Debts bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pmo/debts");
        }
        return this;
    }

    /**
     * Show just one.
     * @param uid User ID
     * @return The XML for one RFP
     * @throws IOException If fails
     */
    public Iterable<Directive> toXembly(final String uid) throws IOException {
        try (final Item item = this.item()) {
            final Iterator<XML> debts = new Xocument(item.path()).nodes(
                String.format("/debts/debt[@login='%s']", uid)
            ).iterator();
            final Directives dirs = new Directives();
            if (debts.hasNext()) {
                final XML debt = debts.next();
                dirs.add("debt")
                    .attr("total", this.amount(uid))
                    .append(Directives.copyOf(debt.node()));
            }
            return dirs;
        }
    }

    /**
     * Add it.
     * @param uid User ID
     * @param amount The amount
     * @param details The details
     * @param reason The reason
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final String uid, final Cash amount,
        final String details, final String reason) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/debts[not(debt[@login='%s'])]", uid))
                    .add("debt").attr("login", uid)
                    .xpath(String.format("/debts/debt[@login='%s']", uid))
                    .strict(1)
                    .addIf("items")
                    .add("item")
                    .add("created").set(new DateAsText().asString()).up()
                    .add("amount").set(amount).up()
                    .add("details").set(details).up()
                    .add("reason").set(reason).up()
            );
        }
    }

    /**
     * The size of the debt.
     * @param uid The owner's login
     * @return The amount
     * @throws IOException If fails
     */
    public Cash amount(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("@%s doesn't have a debt, can't calculate").say(uid)
            );
        }
        try (final Item item = this.item()) {
            return new IoCheckedScalar<>(
                new Reduced<Cash, Cash>(
                    Cash.ZERO,
                    Cash::add,
                    new Mapped<>(
                        Cash.S::new,
                        new Xocument(item.path()).xpath(
                            String.format(
                                "//debt[@login='%s']/items/item/amount/text()",
                                uid
                            )
                        )
                    )
                )
            ).value();
        }
    }

    /**
     * Remove the debt.
     * @param uid The owner's login
     * @throws IOException If fails
     */
    public void remove(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("@%s doesn't have a debt").say(uid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/debts/debt[@login= '%s']", uid))
                    .strict(1)
                    .remove()
            );
        }
    }

    /**
     * Get all users who have debts.
     * @return Logins
     * @throws IOException If fails
     */
    public Collection<String> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath("/debts/debt/@login");
        }
    }

    /**
     * Debt exists?
     * @param uid User ID
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//debt[@login='%s']", uid)
            ).isEmpty();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("debts.xml");
    }

}
