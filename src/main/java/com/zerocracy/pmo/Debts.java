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
package com.zerocracy.pmo;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import org.cactoos.collection.Joined;
import org.cactoos.collection.Sorted;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Debts.
 * <p>
 * When Zerocrat fails to pay, for any reason,
 * the payment amount will be added to 'debts'.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class Debts {

    /**
     * PMO.
     */
    private final Pmo pmo;

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
    public Debts(final Pmo pkt) {
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
                dirs.add("debt").attr("total", this.amount(uid)).append(
                    new Joined<Directive>(
                        new Mapped<XML, Iterable<Directive>>(
                            xml -> {
                                final String details = String.format(
                                    "%s (%s)",
                                    xml.xpath("details/text()").get(0),
                                    xml.xpath("reason/text()").get(0)
                                );
                                final Instant created = Instant.parse(
                                    xml.xpath("created/text()").get(0)
                                );
                                return new Directives().add("item")
                                    .add("ago")
                                    .set(
                                        Logger.format(
                                            "%[ms]s",
                                            Duration.between(
                                                created,
                                                Instant.now()
                                            ).toMillis()
                                        )
                                    )
                                    .up()
                                    .add("amount")
                                    .set(xml.xpath("amount/text()").get(0))
                                    .up()
                                    .add("details")
                                    .set(new Par.ToText(details).toString())
                                    .up()
                                    .add("details_html")
                                    .set(new Par.ToHtml(details).toString())
                                    .up()
                                    .up();
                            },
                            debt.nodes("items/item")
                        )
                    )
                );
                final Iterator<XML> failures = debt.nodes("failure").iterator();
                if (failures.hasNext()) {
                    final XML failure = failures.next();
                    dirs.attr(
                        "failed",
                        String.format(
                            "Failed to pay on %s (attempt #%d)",
                            failure.xpath("created/text()").get(0),
                            Integer.parseInt(
                                failure.xpath("attempt/text()").get(0)
                            )
                        )
                    );
                }
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
        this.add(uid, amount, details, reason, Instant.now());
    }

    /**
     * Add it.
     * @param uid User ID
     * @param amount The amount
     * @param details The details
     * @param reason The reason
     * @param created Created time
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public void add(final String uid, final Cash amount,
        final String details, final String reason, final Instant created)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/debts[not(debt[@login='%s'])]", uid))
                    .add("debt").attr("login", uid)
                    .xpath(String.format("/debts/debt[@login='%s']", uid))
                    .strict(1)
                    .addIf("items")
                    .add("item")
                    .add("created")
                    .set(created.toString())
                    .up()
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
     * Add failure to the debt.
     * @param uid The owner's login
     * @param reason The reason
     * @throws IOException If fails
     */
    public void failure(final String uid,
        final String reason) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("@%s doesn't have a debt, can't add failure").say(uid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/debts/debt[@login='%s' and not(failure)]", uid
                        )
                    )
                    .add("failure")
                    .add("attempt").set(0)
                    .xpath(
                        String.format(
                            "/debts/debt[@login='%s']/failure", uid
                        )
                    )
                    .strict(1)
                    .xpath("attempt").xset(". + 1").up()
                    .addIf("created").set(Instant.now().toString()).up()
                    .addIf("reason").set(reason)
            );
        }
    }

    /**
     * Is it expired and has to be paid now?
     * @param uid The owner's login
     * @return TRUE if yes
     * @throws IOException If fails
     */
    public boolean expired(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("@%s doesn't have a debt, can't check it").say(uid)
            );
        }
        try (final Item item = this.item()) {
            final Instant failed = Instant.parse(
                new Xocument(item.path()).xpath(
                    String.format(
                        "/debts/debt[@login='%s']/failure/created/text()", uid
                    ),
                    "2000-01-01T00:00:00Z"
                )
            );
            return failed.isBefore(Instant.now().minus(Duration.ofDays(1)));
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
     * Check if debt is older than date.
     * @param uid User id
     * @param date Date to compare
     * @return True if older
     * @throws IOException If fails
     */
    public boolean olderThan(final String uid, final Instant date)
        throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("@%s doesn't have a debt, can't check it").say(uid)
            );
        }
        try (final Item item = this.item()) {
            final String xpath = String.format(
                "/debts/debt[@login='%s']/items/item/created/text()", uid
            );
            return new IoCheckedScalar<>(
                new ItemAt<>(
                    new Sorted<>(
                        new Mapped<>(
                            Instant::parse,
                            new Xocument(item.path()).xpath(xpath)
                        )
                    )
                )
            ).value().isBefore(date);
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
