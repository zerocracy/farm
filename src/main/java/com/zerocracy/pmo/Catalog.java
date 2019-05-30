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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.list.SolidList;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Catalog of all projects.
 *
 * @since 1.0
 * @todo #1305:30min Continue replacing old Date classes with Instant.
 *  Remember also to remove instances of `DateAsText` (Instant.toString should
 *  be used). Be careful to ensure Groovy classes are properly updated since
 *  typing is sometimes dodgy in there. There is a lot of classes to change so
 *  try to find a good small cluster of related classes that can be updated.
 * @todo #1453:30min Add new command to handle `sandbox on|off` command
 *  in q-project.xml and stakeholder to change sandbox in catalog.xml
 *  via Catalog.sandbox(...) methods. This stakeholder should allow to change
 *  sandbox flag only to users with role in PMO.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings(
    {
        "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals", "PMD.GodClass"
    }
)
public final class Catalog {

    /**
     * Title.
     */
    private static final String PRJ_TITLE = "title";

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Catalog(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt PMO
     */
    public Catalog(final Project pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Catalog bootstrap() throws IOException {
        try (final Item team = this.item()) {
            new Xocument(team).bootstrap("pmo/catalog");
        }
        return this;
    }

    /**
     * Delete it entirely.
     * @param pid Project ID
     * @throws IOException If fails
     */
    public void delete(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/catalog/project[@id='%s'] ", pid)
                ).strict(1).remove()
            );
        }
    }

    /**
     * Create a project with the given ID.
     * @param pid Project ID
     * @param prefix The prefix
     * @throws IOException If fails
     */
    public void add(final String pid, final String prefix) throws IOException {
        if (this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par("Project %s already exists").say(pid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/catalog")
                    .add("project")
                    .attr("id", pid)
                    .add(Catalog.PRJ_TITLE).set(pid).up()
                    .add("created")
                    .set(new DateAsText().asString()).up()
                    .add("prefix").set(prefix).up()
                    .add("alive").set(true).up()
                    .add("fee").set(Cash.ZERO).up()
                    .add("publish").set(Boolean.toString(false)).up()
                    .add("adviser").set("0crat").up()
                    .add("architect").set("0crat").up()
                    .add("members").up()
                    .add("jobs").set(0).up()
                    .add("orders").set(0).up()
                    .add("cash").attr("deficit", false).set(Cash.ZERO).up()
                    .add("languages").up()
            );
        }
    }

    /**
     * Project's architect.
     * @param pid Project id
     * @return Architect login
     * @throws IOException If fails
     */
    public String architect(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new Xocument(item.path())
                .xpath(
                    String.format(
                        "/catalog/project[@id = '%s']/architect/text()",
                        pid
                    )
                ).iterator().next();
        }
    }

    /**
     * Change project architect.
     * @param pid Project id
     * @param arc New architect
     * @throws IOException If fails
     */
    public void architect(final String pid, final String arc)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .addIf("architect")
                    .set(arc)
            );
        }
    }

    /**
     * Project jobs count.
     * @param pid Project id
     * @return Jobs count
     * @throws IOException If fails
     */
    public int jobs(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return Integer.parseInt(
                new Xocument(item.path())
                    .xpath(
                        String.format(
                            "/catalog/project[@id = '%s']/jobs/text()",
                            pid
                        )
                    ).iterator().next()
            );
        }
    }

    /**
     * Change project jobs count.
     * @param pid Project id
     * @param cnt Jobs count
     * @throws IOException If fails
     */
    public void jobs(final String pid, final int cnt)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .addIf("jobs")
                    .set(cnt)
            );
        }
    }

    /**
     * Project orders count.
     * @param pid Project id
     * @return Architect login
     * @throws IOException If fails
     */
    public int orders(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return Integer.parseInt(
                new Xocument(item.path())
                    .xpath(
                        String.format(
                            "/catalog/project[@id = '%s']/orders/text()",
                            pid
                        )
                    ).iterator().next()
            );
        }
    }

    /**
     * Change project orders count.
     * @param pid Project id
     * @param cnt New order count
     * @throws IOException If fails
     */
    public void orders(final String pid, final int cnt)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .addIf("orders")
                    .set(cnt)
            );
        }
    }

    /**
     * Does project under deficit.
     * @param pid Project id
     * @return TRUE if in deficit
     * @throws IOException If fails
     */
    public boolean deficit(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return Boolean.parseBoolean(
                new Xocument(item.path())
                    .xpath(
                        String.format(
                            "/catalog/project[@id = '%s']/cash/@deficit",
                            pid
                        )
                    ).iterator().next()
            );
        }
    }

    /**
     * Available cash in project.
     * @param pid Project id
     * @return Cash amount
     * @throws IOException If fails
     */
    public Cash cash(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new Cash.S(
                new Xocument(item.path())
                    .xpath(
                        String.format(
                            "/catalog/project[@id = '%s']/cash/text()",
                            pid
                        )
                    ).iterator().next()
            );
        }
    }

    /**
     * Change project cash.
     * @param pid Project id
     * @param cash New cash
     * @param deficit TRUE if under deficit
     * @throws IOException If fails
     */
    public void cash(final String pid, final Cash cash, final boolean deficit)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .push()
                    .xpath("cash")
                    .remove()
                    .pop()
                    .add("cash")
                    .attr("deficit", deficit)
                    .set(cash)
            );
        }
    }

    /**
     * All project members.
     * @param pid Project id
     * @return Members logins
     * @throws IOException If fails
     */
    public Collection<String> members(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/catalog/project[@id='%s']/members/member/text()",
                    pid
                )
            );
        }
    }

    /**
     * All project members.
     * @param pid Project id.
     * @param members Members
     * @throws IOException If fails
     */
    public void members(final String pid, final Iterable<String> members)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .push()
                    .xpath("members/member")
                    .remove()
                    .pop()
                    .addIf("members")
                    .append(
                        new IoCheckedScalar<>(
                            new Reduced<>(
                                new Directives(),
                                (dirs, member) -> dirs
                                    .add("member")
                                    .set(member)
                                    .up(),
                                members
                            )
                        ).value()
                    )
            );
        }
    }

    /**
     * Project's language stack.
     * @param pid Project id
     * @return Languages set
     * @throws IOException If fails
     */
    public Set<String> languages(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new HashSet<>(
                new ListOf<>(
                    new Xocument(item.path()).xpath(
                        String.format(
                            "/catalog/project[@id = '%s']/languages/text()",
                            pid
                        )
                    ).iterator().next().split(",")
                )
            );
        }
    }

    /**
     * Change project language stack.
     * @param pid Project id
     * @param langs New laguages
     * @throws IOException If fails
     */
    public void languages(final String pid, final Set<String> langs)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .addIf("languages")
                    .set(String.join(",", langs))
            );
        }
    }

    /**
     * Project exists?
     * @param pid Project ID
     * @return TRUE if it exists
     * @throws IOException If fails
     */
    public boolean exists(final String pid) throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format("//project[@id  ='%s']", pid)
            ).isEmpty();
        }
    }

    /**
     * Find a project by XPath query.
     * @param xpath XPath query
     * @return Prefixes found, if found
     * @throws IOException If fails
     */
    public Collection<String> findByXPath(final String xpath)
        throws IOException {
        String term = xpath;
        if (!term.isEmpty()) {
            term = String.format("[%s]", term);
        }
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format("//project%s/prefix/text()", term)
            );
        }
    }

    /**
     * Is it on pause?
     * @param pid Project ID
     * @return TRUE if on pause
     * @throws IOException If fails
     */
    public boolean pause(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return !Boolean.parseBoolean(
                new Xocument(item.path()).xpath(
                    String.format(
                        "/catalog/project[@id='%s']/alive/text()",
                        pid
                    )
                ).get(0)
            );
        }
    }

    /**
     * Set it on pause.
     * @param pid Project ID
     * @param pause TRUE if it has to go on pause
     * @throws IOException If fails
     */
    public void pause(final String pid,
        final boolean pause) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/catalog/project[@id='%s']/alive", pid)
                ).strict(1).set(!pause)
            );
        }
    }

    /**
     * Get project fee or Zero.
     * @param pid Project ID
     * @return Per transaction fee
     * @throws IOException If fails
     */
    public Cash fee(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            final Iterator<String> fees = new Xocument(item.path()).xpath(
                String.format("/catalog/project[@id='%s']/fee/text()", pid)
            ).iterator();
            final Cash fee;
            if (fees.hasNext()) {
                fee = new Cash.S(fees.next());
            } else {
                fee = Cash.ZERO;
            }
            return fee;
        }
    }

    /**
     * Set project fee.
     * @param pid Project ID
     * @param fee Fee to set
     * @throws IOException If fails
     */
    public void fee(final String pid, final Cash fee) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/catalog/project[@id='%s']/fee", pid)
                ).strict(1).set(fee)
            );
        }
    }

    /**
     * Publish or unpublish this project.
     * @param pid Project ID
     * @param status Publication success to set
     * @throws IOException If fails
     */
    public void publish(final String pid, final boolean status)
        throws IOException {
        this.checkExist(pid);
        if (this.links(pid, "github").isEmpty()) {
            throw new SoftException(
                new Par(
                    "Project %s is not linked to any GitHub repositories,",
                    "it can't be published on the board, see §26"
                ).say(pid)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/catalog/project[@id='%s']/publish", pid)
                ).strict(1).set(Boolean.toString(status))
            );
        }
    }

    /**
     * This project is published?
     * @param pid Project ID
     * @return TRUE if published
     * @throws IOException If fails
     */
    public boolean published(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return Boolean.parseBoolean(
                new Xocument(item).xpath(
                    String.format(
                        "/catalog/project[@id='%s']/publish/text()",
                        pid
                    )
                ).get(0)
            );
        }
    }

    /**
     * Add a link to the pmo.
     * @param pid Project ID
     * @param rel REL
     * @param href HREF
     * @throws IOException If fails
     */
    public void link(final String pid, final String rel, final String href)
        throws IOException {
        this.checkExist(pid);
        if (this.hasLink(pid, rel, href)) {
            throw new SoftException(
                new Par(
                    "Project %s already has link, rel=`%s`, href=`%s`"
                ).say(pid, rel, href)
            );
        }
        if (this.linkExists(rel, href)) {
            throw new SoftException(
                new Par(
                    "Some other project already has `%s/%s` link"
                ).say(rel, href)
            );
        }
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id='%s']", pid))
                    .strict(1)
                    .addIf("links")
                    .add("link")
                    .attr("rel", rel)
                    .attr("href", href)
            );
        }
    }

    /**
     * Remove a link from the pmo.
     * @param pid Project ID
     * @param rel REL
     * @param href HREF
     * @throws IOException If fails
     */
    public void unlink(final String pid, final String rel, final String href)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id=  '%s']", pid))
                    .strict(1)
                    .xpath(
                        String.format(
                            "links/link[@rel='%s' and @href='%s']",
                            rel, href
                        )
                    )
                    .strict(1)
                    .remove()
            );
        }
    }

    /**
     * Get all project links.
     * @param pid Project ID
     * @return Links found
     * @throws IOException If fails
     */
    public Collection<String> links(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new SolidList<>(
                new Mapped<>(
                    xml -> String.format(
                        "%s:%s",
                        xml.xpath("@rel").get(0),
                        xml.xpath("@href").get(0)
                    ),
                    new Xocument(item).nodes(
                        String.format(
                            "/catalog/project[@id='%s']/links/link",
                            pid
                        )
                    )
                )
            );
        }
    }

    /**
     * Get project links by REL.
     * @param pid Project ID
     * @param rel REL to look for
     * @return Links found
     * @throws IOException If fails
     */
    public Collection<String> links(final String pid, final String rel)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new Xocument(item).xpath(
                String.format(
                    "/catalog/project[@id='%s']/links/link[@rel='%s']/@href",
                    pid, rel
                )
            );
        }
    }

    /**
     * Project has this link?
     * @param pid Project ID
     * @param rel REL
     * @param href HREF
     * @return TRUE if it has a link
     * @throws IOException If fails
     */
    public boolean hasLink(final String pid, final String rel,
        final String href) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "/catalog/project[@id='%s' and links/link[@rel='%s' and @href='%s']]",
                    pid, rel, href
                )
            ).isEmpty();
        }
    }

    /**
     * This link exists in any project?
     * @param rel REL
     * @param href HREF
     * @return TRUE if it exists already
     * @throws IOException If fails
     */
    public boolean linkExists(final String rel, final String href)
        throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item.path()).nodes(
                String.format(
                    "/catalog/project/links/link[@rel='%s' and @href='%s']",
                    rel, href
                )
            ).isEmpty();
        }
    }

    /**
     * Change project title.
     * @param pid Project id
     * @param title New title
     * @throws IOException If fails
     */
    public void title(final String pid, final String title)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id =  '%s']", pid))
                    .strict(1)
                    .addIf(Catalog.PRJ_TITLE)
                    .set(title)
            );
        }
    }

    /**
     * Project title.
     * @param pid Project id
     * @return Title string
     * @throws IOException If fails
     */
    public String title(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            final Iterator<String> items = new Xocument(item.path())
                .xpath(
                    String.format(
                        "/catalog/project[@id = '%s']/title/text()",
                        pid
                    )
                ).iterator();
            String title = pid;
            if (items.hasNext()) {
                title = items.next();
            }
            return title;
        }
    }

    /**
     * Has project adviser.
     *
     * @param pid Project id
     * @return True if has
     * @throws IOException If fails
     */
    public boolean hasAdviser(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            final List<String> xpath = new Xocument(item.path()).xpath(
                String.format(
                    "/catalog/project[@id = '%s']/adviser/text()",
                    pid
                )
            );
            return !xpath.isEmpty() && !"0crat".equals(xpath.get(0));
        }
    }

    /**
     * Project adviser.
     * @param pid Project id
     * @return Adviser id
     * @throws IOException If fails
     */
    public String adviser(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/catalog/project[@id = '%s']/adviser/text()",
                    pid
                )
            ).get(0);
        }
    }

    /**
     * Change project's adviser.
     * @param pid Project id
     * @param adviser Adviser id
     * @throws IOException If fails
     */
    public void adviser(final String pid, final String adviser)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format(
                        "/catalog/project[@id = '%s']",
                        pid
                    )
                ).addIf("adviser").set(adviser)
            );
        }
    }

    /**
     * Checks if project has sandbox flag.
     * @param pid Project ID
     * @return True if project is sandbox project
     * @throws IOException If fails
     */
    public boolean sandbox(final String pid) throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                String.format(
                    "/catalog/project[@id='%s' and sandbox='true']", pid
                )
            ).isEmpty();
        }
    }

    /**
     * Change sandbox flag.
     * @param pid Project ID
     * @param sbx True if sandbox
     * @throws IOException If fails
     */
    public void sandbox(final String pid, final boolean sbx)
        throws IOException {
        this.checkExist(pid);
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id='%s']", pid))
                    .addIf("sandbox")
                    .set(sbx)
            );
        }
    }

    /**
     * Active project IDs.
     * @return Set of project IDs
     * @throws IOException If fails
     */
    public Set<String> active() throws IOException {
        try (final Item item = this.item()) {
            return new HashSet<>(
                new Xocument(item).xpath(
                    "/catalog/project[alive = 'true']/@id"
                )
            );
        }
    }

    /**
     * Check project exists, or throw exception.
     * @param pid Project id
     * @throws IOException If not exist
     */
    private void checkExist(final String pid) throws IOException {
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist"
                ).say(pid)
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("catalog.xml");
    }
}
