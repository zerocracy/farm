/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.StickyList;
import org.xembly.Directives;

/**
 * Catalog of all projects.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class Catalog {

    /**
     * Project.
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
     * @param pkt Project
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
     * Create a project with the given ID.
     * @param pid Project ID
     * @param prefix The prefix
     * @throws IOException If fails
     */
    public void add(final String pid, final String prefix) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/catalog")
                    .add("project")
                    .attr("id", pid)
                    .add("title").set(pid).up()
                    .add("created")
                    .set(
                        ZonedDateTime.now().format(
                            DateTimeFormatter.ISO_INSTANT
                        )
                    )
                    .up()
                    .add("prefix").set(prefix).up()
                    .add("publish").set(Boolean.toString(false))
            );
        }
        Logger.info(
            this, "New project \"%s\" added, prefix is \"%s\"",
            pid, prefix
        );
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
     * Publish or unpublish this project.
     * @param pid Project ID
     * @param status Publication status to set
     * @throws IOException If fails
     */
    public void publish(final String pid, final boolean status)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives().xpath(
                    String.format("/catalog/project[@id='%s']/publish", pid)
                ).strict(1).set(Boolean.toString(status))
            );
        }
        Logger.info(
            this, "Project \"%s\" publishing status changed to \"%s\"",
            pid, status
        );
    }

    /**
     * This project is published?
     * @param pid Project ID
     * @return TRUE if published
     * @throws IOException If fails
     */
    public boolean published(final String pid) throws IOException {
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
        Logger.info(
            this, "Project \"%s\" got a new link, rel=\"%s\", href=\"%s\"",
            pid, rel, href
        );
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
        Logger.info(
            this, "Project \"%s\" lost a link, rel=\"%s\", href=\"%s\"",
            pid, rel, href
        );
    }

    /**
     * Get all project links.
     * @param pid Project ID
     * @return Links found
     * @throws IOException If fails
     */
    public Collection<String> links(final String pid) throws IOException {
        try (final Item item = this.item()) {
            return new StickyList<>(
                new Mapped<>(
                    new Xocument(item).nodes(
                        String.format(
                            "/catalog/project[@id='%s']/links/link",
                            pid
                        )
                    ),
                    xml -> String.format(
                        "%s:%s",
                        xml.xpath("@rel").get(0),
                        xml.xpath("@href").get(0)
                    )
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
     * Set a parent to the pmo.
     * @param pid Project ID
     * @param parent Parent
     * @throws IOException If fails
     */
    public void parent(final String pid, final String parent)
        throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(String.format("/catalog/project[@id='%s' ]", pid))
                    .strict(1)
                    .addIf("parent")
                    .set(parent)
            );
        }
        Logger.info(
            this, "Project \"%s\" got a parent \"%s\"",
            pid, parent
        );
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
