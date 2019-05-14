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
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.NumberOf;
import org.cactoos.scalar.UncheckedScalar;
import org.xembly.Directives;

/**
 * Data about people.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 1.0
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
     * PMO.
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
     * @param pkt PMO
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
     *
     * @param uid User ID
     * @param mentor User ID of the mentor
     * @throws IOException If fails
     */
    public void invite(final String uid, final String mentor)
        throws IOException {
        this.invite(uid, mentor, false);
    }

    /**
     * Invite that person and set a mentor.
     *
     * @param uid User ID
     * @param mentor User ID of the mentor
     * @param force Ignore max student limitation if true
     * @throws IOException If fails
     */
    public void invite(final String uid, final String mentor,
        final boolean force) throws IOException {
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
            if (!force && current >= max) {
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
     * Get user wallet.
     * @param uid User ID
     * @return Wallet of the user or empty string if it's not set
     * @throws IOException If fails
     * @todo #1966:30min Refactor wallet usage, wallet command was removed
     *  from chat bots, so we're sending all payments only via Zold WTS
     *  by Github username. So let's update datum and remove elements from
     *  'people' scheme and remove it from farm then. Same for bank()
     *  method.
     * @checkstyle NonStaticMethodCheck (5 lines)
     */
    public String wallet(final String uid) throws IOException {
        return uid;
    }

    /**
     * Get user bank (like "paypal").
     * @param uid User ID
     * @return Wallet of the user
     * @throws IOException If fails
     * @checkstyle NonStaticMethodCheck (5 lines)
     */
    public String bank(final String uid) throws IOException {
        return "zld";
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
        this.checkExisting(uid);
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
        this.checkExisting(uid);
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
     * Update number of jobs in person's agenda.
     * @param uid User id
     * @param jobs Jobs
     * @throws IOException If fails
     */
    public void jobs(final String uid, final int jobs)
        throws IOException {
        this.checkExisting(uid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/people/person[@id='%s']",
                        uid
                    )
                ).addIf("jobs").set(jobs)
            );
        }
    }

    /**
     * Get number of jobs in person's agenda.
     * @param uid User id
     * @return Number of jobs in agenda
     * @throws IOException If fails
     */
    public int jobs(final String uid) throws IOException {
        this.checkExisting(uid);
        try (final Item item = this.item()) {
            return new NumberOf(
                new Xocument(item.path()).xpath(
                    String.format(
                        "/people/person[@id='%s']/jobs/text()",
                        uid
                    ),
                    "0"
                )
            ).intValue();
        }
    }

    /**
     * Update person's speed.
     * @param uid User id
     * @param speed Speed
     * @throws IOException If fails
     */
    public void speed(final String uid, final double speed)
        throws IOException {
        this.checkExisting(uid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/people/person[@id='%s']",
                        uid
                    )
                ).addIf("speed").set(speed)
            );
        }
    }

    /**
     * Get person's speed.
     * @param uid User id
     * @return Speed
     * @throws IOException If fails
     */
    public double speed(final String uid) throws IOException {
        this.checkExisting(uid);
        try (final Item item = this.item()) {
            return new NumberOf(
                new Xocument(item.path()).xpath(
                    String.format(
                        "/people/person[@id='%s']/speed/text()",
                        uid
                    ),
                    "0.0"
                )
            ).doubleValue();
        }
    }

    /**
     * Person exists?
     * @param uid User ID
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//people/person[@id  ='%s']", uid)
            ).isEmpty();
        }
    }

    /**
     * Apply.
     * @param uid User id
     * @param when When applied (UTC)
     * @throws IOException If fails
     */
    public void apply(final String uid, final Instant when) throws IOException {
        this.checkExisting(uid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("//people/person[@id  ='%s']", uid)
                ).addIf("applied").set(when.toString())
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
        this.checkExisting(uid);
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
    public Instant appliedTime(final String uid) throws IOException {
        this.checkExisting(uid);
        if (!this.applied(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't have apply-time").say(uid)
            );
        }
        try (final Item item = this.item()) {
            return Instant.parse(
                new Xocument(item).xpath(
                    String.format(
                        "//people/person[@id  ='%s']/applied/text()",
                        uid
                    )
                ).get(0)
            );
        }
    }

    /**
     * Skills of a person.
     * @param user Person's login
     * @return Iterable with skills
     * @throws IOException If fails
     */
    public Iterable<String> skills(final String user) throws IOException {
        try (final Item item = this.item()) {
            return new Mapped<>(
                xml -> xml.node().getTextContent(),
                new Xocument(item.path()).nodes(
                    String.format(
                        "/people/person[@id = '%s']/skills/skill",
                        user
                    )
                )
            );
        }
    }

    /**
     * Update the list of user skills.
     * @param uid User id
     * @param skills List of skills
     * @throws IOException If fails
     */
    public void skills(final String uid, final Iterable<String> skills)
        throws IOException {
        this.checkExisting(uid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/people/person[@id='%s']/skills",
                        uid
                    )
                )
                    .remove()
                    .add("skills").attr("updated", Instant.now())
                    .append(
                        new Joined<>(
                            new Mapped<>(
                                skill -> new Directives().add("skill")
                                    .set(skill).up(),
                                skills
                            )
                        )
                    )
            );
        }
    }

    /**
     * Active users IDs (with reputation > 256).
     * @return Active users IDs set
     * @throws IOException If fails
     */
    public Set<String> active() throws IOException {
        try (final Item item = this.item()) {
            return new HashSet<>(
                new Xocument(item).xpath(
                    "/people/person[reputation > 256]/@id"
                )
            );
        }
    }

    /**
     * Visible users (with mentor and non-zero reputation).
     * @return Visible users set
     * @throws IOException If fails
     */
    public Set<String> visible() throws IOException {
        try (final Item item = this.item()) {
            return new HashSet<>(
                new Xocument(item).xpath(
                    "/people/person[mentor and reputation > 0]/@id"
                )
            );
        }
    }

    /**
     * Total reputation of all visible users.
     * @return Reputation number
     * @throws IOException If fails
     */
    public int totalReputation() throws IOException {
        try (final Item item = this.item()) {
            return new NumberOf(
                new Xocument(item).xpath(
                    "sum(/people/person[mentor and reputation >0]/reputation)",
                    "0"
                )
            ).intValue();
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
            .add("speed").set("0.0").up()
            .add("skills").attr("updated", Instant.now()).up()
            .add("active").set(true).up()
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

    /**
     * Check if user exists.
     * @param uid UID of user
     * @throws IOException If fails
     */
    private void checkExisting(final String uid) throws IOException {
        if (!this.exists(uid)) {
            throw new IllegalArgumentException(
                new Par("Person @%s doesn't exist").say(uid)
            );
        }
    }
}
