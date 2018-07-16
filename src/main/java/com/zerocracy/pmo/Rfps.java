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
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.JoinedText;
import org.cactoos.time.DateAsText;
import org.cactoos.time.DateOf;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * All RFPs (requests for proposal).
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class Rfps {

    /**
     * PMO.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Rfps(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public Rfps(final Pmo pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Rfps bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pmo/rfps");
        }
        return this;
    }

    /**
     * List them all.
     * @return List of all RFPs
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<Directive> toXembly() throws IOException {
        try (final Item item = this.item()) {
            final Directives dirs = new Directives().add("rfps");
            for (final XML rfp
                : new Xocument(item.path()).nodes("//rfp[sow!='']")) {
                dirs.add("rfp")
                    .add("id").set(rfp.xpath("@id").get(0)).up()
                    .add("created").set(rfp.xpath("created/text()").get(0)).up()
                    .add("ago")
                    .set(
                        Logger.format(
                            "%[ms]s",
                            System.currentTimeMillis()
                            - new DateOf(
                                rfp.xpath("created/text()").get(0)
                            ).value().getTime()
                        )
                    )
                    .up()
                    .add("sow");
                final Iterator<String> sow = rfp.xpath("sow/text()").iterator();
                if (sow.hasNext()) {
                    dirs.set(sow.next());
                } else {
                    dirs.set("");
                }
                dirs.up().up();
            }
            return dirs;
        }
    }

    /**
     * Show just one.
     * @param uid User ID
     * @return The XML for one RFP
     * @throws IOException If fails
     */
    public Iterable<Directive> toXembly(final String uid) throws IOException {
        try (final Item item = this.item()) {
            final XML rfp = new Xocument(item.path()).nodes(
                String.format("//rfp[login='%s']", uid)
            ).get(0);
            final Directives dirs = new Directives().add("rfp")
                .add("id").set(rfp.xpath("@id").get(0)).up()
                .add("created").set(rfp.xpath("created/text()").get(0)).up()
                .add("paid").set(rfp.xpath("paid/text()").get(0)).up()
                .add("email").set(rfp.xpath("email/text()").get(0)).up()
                .add("sow");
            final Iterator<String> sow = rfp.xpath("sow/text()").iterator();
            if (sow.hasNext()) {
                dirs.set(sow.next());
            } else {
                dirs.set("");
            }
            return dirs.up().up();
        }
    }

    /**
     * Buy it.
     * @param rid RFP id
     * @param buyer The buyer
     * @return Email
     * @throws IOException If fails
     */
    public String buy(final int rid, final String buyer) throws IOException {
        if (!this.exists(rid)) {
            throw new IllegalArgumentException(
                new Par(
                    "RFP for %d doesn't exist for @%s, can't buy"
                ).say(rid, buyer)
            );
        }
        try (final Item item = this.item()) {
            final String email = new Xocument(item.path()).xpath(
                String.format("/rfps/rfp[@id='%s']/email/text()", rid)
            ).get(0);
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/rfps/rfp[@id='%s']", rid))
                    .strict(1).remove()
            );
            return email;
        }
    }

    /**
     * Pay for new RFP.
     * @param login The owner's login
     * @param payment Payment information
     * @param email The email of the owner
     * @return RFP ID
     * @throws IOException If fails
     */
    public int pay(final String login, final String payment,
        final String email) throws IOException {
        if (this.exists(login)) {
            throw new IllegalArgumentException(
                new Par("RFP exists already, no need to pay again").say()
            );
        }
        try (final Item item = this.item()) {
            final int max = Integer.parseInt(
                new Xocument(item).xpath("/rfps/@max", "0")
            );
            final int rid = max + 1;
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/rfps")
                    .attr("max", rid)
                    .add("rfp")
                    .attr("id", rid)
                    .add("created")
                    .set(new DateAsText().asString()).up()
                    .add("login").set(login).up()
                    .add("paid").set(payment).up()
                    .add("email").set(email).up()
                    .add("sow").set("").up()
            );
            return rid;
        }
    }

    /**
     * Post RFP information.
     * @param login The owner's login
     * @param sow Statement of work
     * @return ID of the RFP
     * @throws IOException If fails
     */
    public int post(final String login, final String sow) throws IOException {
        if (!this.exists(login)) {
            throw new IllegalArgumentException(
                new Par("RFP doesn't exist, you need to pay first").say()
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/rfps/rfp[login='%s']/sow", login)
                ).strict(1).set(sow).up()
            );
            return Integer.parseInt(
                new Xocument(item).xpath(
                    String.format("//rfp[login= '%s']/@id", login)
                ).get(0)
            );
        }
    }

    /**
     * Get RFP owner.
     * @param rid RFP ID
     * @return Owner, user login
     * @throws IOException If fails
     */
    public String owner(final int rid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format("//rfp[@id='%s']/login/text()", rid)
            ).get(0);
        }
    }

    /**
     * RFP exists for this ID?
     * @param rid RFP ID
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final int rid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//rfp[@id='%s']", rid)
            ).isEmpty();
        }
    }

    /**
     * RFP exists for this owner?
     * @param uid User ID
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//rfp[login='%s']", uid)
            ).isEmpty();
        }
    }

    /**
     * RFP exists for this owner and contains SOW?
     * @param uid User ID
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean complete(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//rfp[login='%s' and sow!='']", uid)
            ).isEmpty();
        }
    }

    /**
     * Find all rfps where created date earlier than data param.
     * @param date Date
     * @return Rfp ids
     * @throws IOException If fails
     */
    public Iterable<Integer> olderThan(final Date date) throws IOException {
        try (final Item item = this.item()) {
            return new Mapped<>(
                Integer::parseInt,
                new Xocument(item.path()).xpath(
                    new JoinedText(
                        "",
                        "/rfps/rfp[xs:dateTime(created) < ",
                        "xs:dateTime('",
                        new DateAsText(date).asString(),
                        "')]/@id"
                    ).asString()
                )
            );
        }
    }

    /**
     * Remove RFP by id.
     * @param rfp Rfp id
     * @throws IOException If fails
     */
    public void remove(final Long rfp) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/rfps/rfp[@id = '%d']", rfp)
                ).remove()
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("rfps.xml");
    }
}
