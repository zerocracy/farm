/**
 * Copyright (c) 2016 Zerocracy
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
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.cash.Cash;
import com.zerocracy.jstk.cash.Currency;
import com.zerocracy.stk.SoftException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.xembly.Directives;

/**
 * Data about people.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class People {

    /**
     * Project.
     */
    private final Project project;

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
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @throws IOException If fails
     */
    public void bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("people", "pmo/people");
        }
    }

    /**
     * Add new skill.
     * @param uid User ID
     * @param skill The skill to add
     * @throws IOException If fails
     */
    public void skill(final String uid, final String skill) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("skills")
                    .add("skill")
                    .set(skill)
            );
        }
    }

    /**
     * Get all user skills.
     * @param uid User ID
     * @return List of skills
     * @throws IOException If fails
     */
    public Collection<String> skills(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format(
                    "/people/person[@id='%s']/skills/skill/text()",
                    uid
                )
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
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                People.start(uid)
                    .addIf("rate")
                    .set(rate.exchange(Currency.USD).decimal().toPlainString())
                    .attr("currency", Currency.USD.code())
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
            if (!rates.hasNext()) {
                throw new SoftException(
                    String.format(
                        "Your rate is not set yet (uid=%s)", uid
                    )
                );
            }
            final XML rate = rates.next();
            return new Cash.S(
                String.format(
                    "%s %s",
                    rate.xpath("@currency").get(0),
                    rate.xpath("text()").get(0)
                )
            );
        }
    }

    /**
     * Set wallet.
     * @param uid User ID
     * @param bank Bank
     * @param wallet Wallet value
     * @throws IOException If fails
     */
    public void wallet(final String uid, final String bank,
        final String wallet) throws IOException {
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
     * @return Wallet of the user
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
            if (!wallet.hasNext()) {
                throw new SoftException(
                    String.format(
                        "Your wallet is not set yet (uid=%s)", uid
                    )
                );
            }
            return wallet.next();
        }
    }

    /**
     * Get user wallet.
     * @param uid User ID
     * @return Wallet of the user
     * @throws IOException If fails
     */
    public String bank(final String uid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/people/person[@id='%s']/wallet/@bank",
                    uid
                )
            ).iterator().next();
        }
    }

    /**
     * Add alias.
     *
     * <p>There can be multiple aliases for a single user ID. Each alias
     * comes from some other system, where that user is present. For example,
     * "email", "twitter", "github", "jira", etc.
     *
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
            return new Xocument(item).nodes(
                String.format(
                    "/people/person[@id='%s']/links/link",
                    uid
                )
            ).stream().map(
                xml -> String.format(
                    "%s:%s",
                    xml.xpath("@rel").get(0),
                    xml.xpath("@href").get(0)
                )
            ).collect(Collectors.toList());
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("people.xml");
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
            .xpath(
                String.format(
                    "/people/person[@id='%s']",
                    uid
                )
            )
            .strict(1);
    }
}
