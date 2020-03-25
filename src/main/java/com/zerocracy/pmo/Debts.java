/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.ItemXml;
import com.zerocracy.Par;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.cactoos.collection.Joined;
import org.cactoos.collection.Sorted;
import org.cactoos.io.InputOf;
import org.cactoos.io.Sha256DigestOf;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.cactoos.text.HexOf;
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
 * @checkstyle LineLengthCheck (5000 lines)
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
     */
    public Debts bootstrap() {
        return this;
    }

    /**
     * Show just one.
     * @param uid User ID
     * @return The XML for one RFP
     * @throws IOException If fails
     */
    public Iterable<Directive> toXembly(final String uid) throws IOException {
        final List<XML> nodes = this.item().nodes(String.format("/debts/debt[@login='%s']", uid));
        final Iterator<XML> debts = nodes.iterator();
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
        this.item().update(
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

    /**
     * The size of the debt.
     * @param uid The owner's login
     * @return The amount
     * @throws IOException If fails
     */
    public Cash amount(final String uid) throws IOException {
        return new IoCheckedScalar<>(
            new Reduced<Cash, Cash>(
                Cash.ZERO,
                Cash::add,
                new Mapped<>(
                    Cash.S::new,
                    this.item().<List<String>>read(
                        xoc -> Debts.require(xoc, uid).xpath(
                            String.format(
                                "//debt[@login='%s']/items/item/amount/text()",
                                uid
                            )
                        )
                    )
                )
            )
        ).value();
    }

    /**
     * Remove the debt.
     * @param uid The owner's login
     * @throws IOException If fails
     */
    public void remove(final String uid) throws IOException {
        this.item().update(
            xoc -> Debts.require(xoc, uid).modify(
                new Directives()
                    .xpath(String.format("/debts/debt[@login= '%s']", uid))
                    .strict(1)
                    .remove()
            )
        );
    }

    /**
     * Add failure to the debt.
     * @param uid The owner's login
     * @param reason The reason
     * @throws IOException If fails
     */
    public void failure(final String uid,
        final String reason) throws IOException {
        this.item().update(
            xoc -> Debts.require(xoc, uid).modify(
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
            )
        );
    }

    /**
     * Is it expired and has to be paid now?
     * @param uid The owner's login
     * @return TRUE if yes
     * @throws IOException If fails
     */
    public boolean expired(final String uid) throws IOException {
        final Instant failed = Instant.parse(
            this.item().read(
                xoc -> Debts.require(xoc, uid).xpath(
                    String.format(
                        "/debts/debt[@login='%s']/failure/created/text()", uid
                    ),
                    "2000-01-01T00:00:00Z"
                )
            )
        );
        return failed.isBefore(Instant.now().minus(Duration.ofDays(1)));
    }

    /**
     * Get all users who have debts.
     * @param filter Additional filter
     * @return Logins
     * @throws IOException If fails
     */
    public Collection<String> iterate(final String filter)
        throws IOException {
        return this.item().xpath(
            String.format("/debts/debt[%s]/@login", filter)
        );
    }

    /**
     * Debt exists?
     * @param uid User ID
     * @return TRUE if exists
     * @throws IOException If fails
     */
    public boolean exists(final String uid) throws IOException {
        return this.item().read(xoc -> Debts.exists(xoc, uid));
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
        return new IoCheckedScalar<>(
            new ItemAt<>(
                new Sorted<>(
                    new Mapped<>(
                        Instant::parse,
                        this.item().<List<String>>read(
                            xoc -> Debts.require(xoc, uid).xpath(
                                String.format(
                                    "/debts/debt[@login='%s']/items/item/created/text()", uid
                                )
                            )
                        )
                    )
                )
            )
        ).value().isBefore(date);
    }

    /**
     * Hash of all debts.
     * @param uid Login
     * @return Hash string
     * @throws IOException If fails
     */
    public String hash(final String uid) throws IOException {
        final String str = new IoCheckedScalar<>(
            new Reduced<>(
                new StringBuilder(Tv.HUNDRED),
                (acc, debt) -> acc.append(
                    String.join(
                        "",
                        debt.xpath("created/text()").get(0),
                        debt.xpath("amount/text()").get(0),
                        debt.xpath("details/text()").get(0),
                        debt.xpath("reason/text()").get(0)
                    )
                ),
                this.item().read(
                    xoc -> Debts.require(xoc, uid).nodes(
                        String.format(
                            "/debts/debt[@login='%s']/items/item", uid
                        )
                    )
                )
            )
        ).value().toString();
        return new HexOf(
            new Sha256DigestOf(new InputOf(str, StandardCharsets.UTF_8))
        ).asString();
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private ItemXml item() throws IOException {
        return new ItemXml(this.pmo.acq("debts.xml"), "pmo/debts");
    }

    /**
     * Require debt in xocument?
     * @param xoc Xocument
     * @param uid User ID
     * @return Xocument
     * @throws IOException If fails
     */
    private static Xocument require(final Xocument xoc, final String uid)
        throws IOException {
        if (!Debts.exists(xoc, uid)) {
            throw new IllegalArgumentException(
                new Par("@%s doesn't have a debt, can't check it").say(uid)
            );
        }
        return xoc;
    }

    /**
     * Debt exists in xocument?
     * @param xoc Xocument
     * @param uid User ID
     * @return TRUE if exists
     * @throws IOException If fails
     */
    private static boolean exists(final Xocument xoc, final String uid)
        throws IOException {
        return !xoc.nodes(String.format("//debt[@login='%s']", uid)).isEmpty();
    }
}
