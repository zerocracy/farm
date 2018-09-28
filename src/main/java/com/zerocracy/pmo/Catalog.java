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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.cactoos.collection.CollectionOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.SolidList;
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
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class Catalog {

    /**
     * Title.
     */
    private static final String PRJ_TITLE = "title";

    /**
     * PMO.
     */
    private final Pmo pmo;

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
    public Catalog(final Pmo pkt) {
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
     * Return all sandbox projects.
     * @return List of sandbox projects
     * @checkstyle NonStaticMethodCheck (3 lines)
     */
    public Collection<String> sandbox() {
        return new CollectionOf<>(
            "CAZPZR9FS", "C63314D6Z", "C7JGJ00DP"
        );
    }

    /**
     * Delete it entirely.
     * @param pid Project ID
     * @throws IOException If fails
     */
    public void delete(final String pid) throws IOException {
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par("Project %s doesn't exist, can't delete").say(pid)
            );
        }
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
                    .add("adviser").set("0crat")
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par("Project %s doesn't exist").say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par("Project %s doesn't exist, can't pause").say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't get fee"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't set fee"
                ).say(pid)
            );
        }
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
        if (this.links(pid, "github").isEmpty()) {
            throw new SoftException(
                new Par(
                    "Project %s is not linked to any GitHub repositories,",
                    "it can't be published on the board, see §26"
                ).say(pid)
            );
        }
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't publish, see §26"
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project \"%s\" doesn't exist, can't check publish"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't add link"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't unlink"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't get links"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't get links"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't check link"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't change title"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't get title"
                ).say(pid)
            );
        }
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
     * Project adviser.
     * @param pid Project id
     * @return Adviser id
     * @throws IOException If fails
     */
    public String adviser(final String pid) throws IOException {
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't get adviser"
                ).say(pid)
            );
        }
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
        if (!this.exists(pid)) {
            throw new IllegalArgumentException(
                new Par(
                    "Project %s doesn't exist, can't get adviser"
                ).say(pid)
            );
        }
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
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("catalog.xml");
    }
}
