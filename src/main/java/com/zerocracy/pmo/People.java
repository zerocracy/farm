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
import com.zerocracy.Policy;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.NumberOf;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.time.DateAsText;
import org.cactoos.time.DateOf;
import org.xembly.Directives;

/**
 * Data about people.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.1
 * @todo #552:30min Let's keep agenda inside `people.xml` and update it
 *  when agenda changed as described in
 *  https://github.com/zerocracy/farm/issues/366#issuecomment-359568311
 *  Also put it as 'agenda' element in TkGang xml.
 */
@SuppressWarnings
    (
        {
            "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals",
            "PMD.NPathComplexity", "PMD.CyclomaticComplexity"
        }
    )
public final class People {

    /**
     * Project.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public People(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public People(final Project pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public People bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/people");
        }
        return this;
    }

    /**
     * Get them all.
     * @return List of them
     * @throws IOException If fails
     */
    public Iterable<String> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                "/people/person/@id"
            );
        }
    }

    /**
     * Remove person.
     * @param uid Person id
     * @throws IOException If fails
     */
    public void remove(final String uid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/people/person[@id='%s']", uid)
                ).remove()
            );
        }
    }

    /**
     * Touch this dude.
     * @param uid User ID
     * @throws IOException If fails
     */
    public void touch(final String uid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
            );
        }
    }

    /**
     * Set mentor to '0crat'.
     * @param uid User ID
     * @throws IOException If fails
     */
    public void graduate(final String uid) throws IOException {
        if (!this.hasMentor(uid)) {
            throw new SoftException(
                new Par(
                    "User @%s is not with us yet"
                ).say(uid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("mentor")
                    .set("0crat")
            );
        }
    }

    /**
     * Set details.
     * @param uid User ID
     * @param text Text to save
     * @throws IOException If fails
     */
    public void details(final String uid, final String text)
        throws IOException {
        if (!this.hasMentor(uid)) {
            throw new SoftException(
                new Par(
                    "User @%s is not with us yet"
                ).say(uid)
            );
        }
        if (text.isEmpty()) {
            throw new SoftException(
                new Par(
                    "User @%s details can't be empty"
                ).say(uid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("details")
                    .set(text)
            );
        }
    }

    /**
     * Get user details.
     * @param uid User ID
     * @return Details of the user
     * @throws IOException If fails
     */
    public String details(final String uid) throws IOException {
        try (final Item item = this.item()) {
            final Iterator<String> items = new Xocument(item.path()).xpath(
                String.format(
                    "/people/person[@id='%s']/details/text()",
                    uid
                )
            ).iterator();
            final String text;
            if (items.hasNext()) {
                text = items.next();
            } else {
                text = "";
            }
            return text;
        }
    }

    /**
     * Invite that person and set a mentor.
     * @param uid User ID
     * @param mentor User ID of the mentor
     * @throws IOException If fails
     */
    public void invite(final String uid, final String mentor)
        throws IOException {
        if (this.hasMentor(uid)) {
            throw new SoftException(
                new Par(
                    "@%s is already with us, no need to invite again"
                ).say(uid)
            );
        }
        try (final Item item = this.item()) {
            final int max = new Policy().get("1.max-students", 16);
            final int current = new Xocument(item.path())
                .nodes(
                    String.format(
                        "/people/person[mentor/text()='%s']",
                        mentor
                    )
                ).size();
            if (current >= max) {
                throw new SoftException(
                    new Par(
                        "You can not invite more than %d students;",
                        "you already have %d, see §1"
                    ).say(max, current)
                );
            }
            new Xocument(item.path()).modify(
                People.start(uid)
                    .push()
                    .xpath("mentor")
                    .strict(0)
                    .pop()
                    .add("mentor")
                    .set(mentor)
            );
        }
    }

    /**
     * This person has a mentor?
     * @param uid User ID
     * @return TRUE if he has a mentor
     * @throws IOException If fails
     */
    public boolean hasMentor(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format(
                    "/people/person[@id='%s']/mentor",
                    uid
                )
            ).isEmpty();
        }
    }

    /**
     * Person's mentor.
     * @param uid User ID
     * @return Id of person's mentor
     * @throws IOException If fails
     */
    public String mentor(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/people/person[@id='%s']/mentor/text()",
                    uid
                )
            ).get(0);
        }
    }

    /**
     * Breakup with a person.
     * @param uid User ID
     * @throws IOException If fails
     */
    public void breakup(final String uid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/people/person[@id='%s']/mentor", uid)
                ).remove()
            );
        }
    }

    /**
     * Set rate.
     * @param uid User ID
     * @param rate Rate of the user
     * @throws IOException If fails
     */
    public void rate(final String uid, final Cash rate) throws IOException {
        final Cash max = new Policy().get("16.max", new Cash.S("$256"));
        if (rate.compareTo(max) > 0) {
            throw new SoftException(
                new Par(
                    "This is too high (%s),",
                    "we do not work with rates higher than %s, see §16"
                ).say(rate, max)
            );
        }
        final Cash min = new Policy().get("16.min", Cash.ZERO);
        if (rate.compareTo(min) < 0) {
            throw new SoftException(
                new Par(
                    "This is too low (%s),",
                    "we do not work with rates lower than %s, see §16"
                ).say(rate, min)
            );
        }
        if (this.wallet(uid).isEmpty()) {
            throw new SoftException(
                new Par(
                    "You must configure your wallet first,",
                    "before setting the rate to %s, see §20"
                ).say(rate)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("rate")
                    .set(rate)
            );
        }
    }

    /**
     * Get user rate.
     * @param uid User ID
     * @return Rate of the user
     * @throws IOException If fails
     */
    public Cash rate(final String uid) throws IOException {
        try (final Item item = this.item()) {
            final Iterator<XML> rates = new Xocument(item.path()).nodes(
                String.format(
                    "/people/person[@id='%s']/rate",
                    uid
                )
            ).iterator();
            final String rate;
            if (rates.hasNext()) {
                rate = rates.next().xpath("text()").get(0);
            } else {
                rate = Cash.ZERO.toString();
            }
            return new Cash.S(rate);
        } catch (final CashParsingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Set wallet.
     * @param uid User ID
     * @param bank Bank
     * @param wallet Wallet value
     * @throws IOException If fails
     * @checkstyle CyclomaticComplexityCheck (100 lines)
     */
    public void wallet(final String uid, final String bank,
        final String wallet) throws IOException {
        if (!bank.matches("paypal|btc|bch|eth|ltc")) {
            throw new SoftException(
                new Par(
                    "Bank name `%s` is invalid, we accept only",
                    "`paypal`, `btc`, `bch`, `eth`, or `ltc`, see §20"
                ).say(bank)
            );
        }
        if ("paypal".equals(wallet)
            && !wallet.matches("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b")) {
            throw new SoftException(
                new Par("Email `%s` is not valid").say(wallet)
            );
        }
        if ("btc".equals(wallet)
            && !wallet.matches("(1|3|bc1)[a-zA-Z0-9]{20,40}")) {
            throw new SoftException(
                new Par("Bitcoin address is not valid: `%s`").say(wallet)
            );
        }
        if ("bch".equals(wallet)
            && !wallet.matches("[pq]{41}")) {
            throw new SoftException(
                new Par("Bitcoin Cash address is not valid: `%s`").say(wallet)
            );
        }
        if ("eth".equals(wallet)
            && !wallet.matches("[0-9a-f]{42}")) {
            throw new SoftException(
                new Par("Etherium address is not valid: `%s`").say(wallet)
            );
        }
        if ("ltc".equals(wallet)
            && !wallet.matches("[0-9a-zA-Z]{35}")) {
            throw new SoftException(
                new Par("Litecoin address is not valid: `%s`").say(wallet)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("wallet")
                    .set(wallet)
                    .attr("bank", bank)
            );
        }
    }

    /**
     * Get user wallet.
     * @param uid User ID
     * @return Wallet of the user or empty string if it's not set
     * @throws IOException If fails
     */
    public String wallet(final String uid) throws IOException {
        try (final Item item = this.item()) {
            final Iterator<String> wallet = new Xocument(item.path()).xpath(
                String.format(
                    "/people/person[@id='%s']/wallet/text()",
                    uid
                )
            ).iterator();
            final String value;
            if (wallet.hasNext()) {
                value = wallet.next();
            } else {
                value = "";
            }
            return value;
        }
    }

    /**
     * Get user bank (like "paypal").
     * @param uid User ID
     * @return Wallet of the user
     * @throws IOException If fails
     */
    public String bank(final String uid) throws IOException {
        try (final Item item = this.item()) {
            final Iterator<String> banks = new Xocument(item.path()).xpath(
                String.format(
                    "/people/person[@id='%s']/wallet/@bank",
                    uid
                )
            ).iterator();
            final String value;
            if (banks.hasNext()) {
                value = banks.next();
            } else {
                value = "";
            }
            return value;
        }
    }

    /**
     * Add alias.
     *
     * <p>There can be multiple aliases for a single user ID. Each alias
     * comes from some other system, where that user is present. For example,
     * "email", "twitter", "github", "jira", etc.
     * @param uid User ID
     * @param rel REL for the alias, e.g. "github"
     * @param alias Alias, e.g. "yegor256"
     * @throws IOException If fails
     */
    public void link(final String uid, final String rel,
        final String alias) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("links")
                    .add("link")
                    .attr("rel", rel)
                    .attr("href", alias)
            );
        }
    }

    /**
     * Find user ID by alias.
     * @param rel REL
     * @param alias Alias
     * @return Found user ID or empty iterable
     * @throws IOException If fails
     */
    public Iterable<String> find(final String rel,
        final String alias) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format(
                    "/people/person[links/link[@rel='%s' and @href='%s']]/@id",
                    rel, alias
                )
            );
        }
    }

    /**
     * Get all aliases of a user.
     * @param uid User ID
     * @return Aliases found
     * @throws IOException If fails
     */
    public Iterable<String> links(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return new Mapped<>(
                xml -> String.format(
                    "%s:%s",
                    xml.xpath("@rel").get(0),
                    xml.xpath("@href").get(0)
                ),
                new Xocument(item).nodes(
                    String.format(
                        "/people/person[@id='%s']/links/link",
                        uid
                    )
                )
            );
        }
    }

    /**
     * Get all aliases of a user by fixed REL.
     * @param uid User ID
     * @param rel The REL
     * @return HREFs found
     * @throws IOException If fails
     */
    public Iterable<String> links(final String uid, final String rel)
        throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format(
                    "/people/person[@id='%s']/links/link[@rel='%s']/@href",
                    uid, rel
                )
            );
        }
    }

    /**
     * Get single link by REL.
     * @param uid User id
     * @param rel Link rel
     * @return Single link href
     * @throws IOException If there is no links or too many
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    public String link(final String uid, final String rel)
        throws IOException {
        final Iterator<String> links = this.links(uid, rel).iterator();
        if (!links.hasNext()) {
            throw new IOException(
                String.format("No such link '%s' for '%s'", rel, uid)
            );
        }
        final String link = links.next();
        if (links.hasNext()) {
            throw new IOException(
                String.format("Too many links '%s' for '%s'", rel, uid)
            );
        }
        return link;
    }

    /**
     * Set vacation mode.
     * @param uid User ID
     * @param mode TRUE if vacation mode on
     * @throws IOException If fails
     */
    public void vacation(final String uid,
        final boolean mode) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("vacation")
                    .set(mode)
            );
        }
    }

    /**
     * Check vacation mode.
     * @param uid User ID
     * @return TRUE if person on vacation
     * @throws IOException If fails
     */
    public boolean vacation(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return new UncheckedScalar<>(
                new ItemAt<>(
                    false,
                    new Mapped<>(
                        Boolean::parseBoolean,
                        new Xocument(item).xpath(
                            String.format(
                                "/people/person[@id='%s']/vacation/text()",
                                uid
                            )
                        )
                    )
                )
            ).value();
        }
    }

    /**
     * Students of a person.
     * @param uid Person's login
     * @return Iterable with student ids
     * @throws IOException If fails
     */
    public Iterable<String> students(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/people/person[mentor/text()='%s']/@id",
                    uid
                )
            );
        }
    }

    /**
     * Update person reputation.
     * @param uid User id
     * @param rep Reputation
     * @throws IOException If fails
     */
    public void reputation(final String uid, final int rep)
        throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't exist").say(uid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/people/person[@id='%s']",
                        uid
                    )
                ).addIf("reputation").set(rep)
            );
        }
    }

    /**
     * Get person reputation.
     * @param uid User id
     * @return Reputation
     * @throws IOException If fails
     */
    public int reputation(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't exist").say(uid)
            );
        }
        try (final Item item = this.item()) {
            return new NumberOf(
                new Xocument(item.path()).xpath(
                    String.format(
                        "/people/person[@id='%s']/reputation/text()",
                        uid
                    ),
                    "0"
                )
            ).intValue();
        }
    }

    /**
     * Person exists?
     * @param pid Person ID
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String pid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//people/person[@id  ='%s']", pid)
            ).isEmpty();
        }
    }

    /**
     * Apply.
     * @param uid User id
     * @param when When applied (UTC)
     * @throws IOException If fails
     */
    public void apply(final String uid, final Date when) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't exist").say(uid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("//people/person[@id  ='%s']", uid)
                ).addIf("applied").set(new DateAsText(when).asString())
            );
        }
    }

    /**
     * Does user apply.
     * @param uid User id
     * @return True if applied
     * @throws IOException If fails
     */
    public boolean applied(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't exist").say(uid)
            );
        }
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//people/person[@id  ='%s']/applied", uid)
            ).isEmpty();
        }
    }

    /**
     * When user applied.
     * @param uid User id
     * @return Applied time (UTC)
     * @throws IOException If fails
     */
    public Date appliedTime(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't exist").say(uid)
            );
        }
        if (!this.applied(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't have apply-time").say(uid)
            );
        }
        try (final Item item = this.item()) {
            return new DateOf(
                new Xocument(item).xpath(
                    String.format(
                        "//people/person[@id  ='%s']/applied/text()",
                        uid
                    )
                ).get(0)
            ).value();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("people.xml");
    }

    /**
     * Start directives, to make sure this user is in XML.
     * @param uid User ID
     * @return Directives
     */
    private static Directives start(final String uid) {
        return new Directives()
            .xpath(
                String.format(
                    "/people[not(person[@id='%s'])]",
                    uid
                )
            )
            .add("person")
            .attr("id", uid)
            .add("reputation").set("0").up()
            .add("jobs").set("0").up()
            .add("projects").set("0").up()
            .add("skills").attr("updated", new DateAsText().asString()).up()
            .add("links")
            .add("link")
            .attr("rel", "github")
            .attr("href", uid)
            .xpath(
                String.format(
                    "/people/person[@id='%s']",
                    uid
                )
            )
            .strict(1);
    }
}
