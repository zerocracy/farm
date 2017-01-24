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

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import org.xembly.Directives;

/**
 * Catalog of all projects.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Catalog {

    /**
     * Project.
     */
    private final Project project;

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
        this.project = pkt;
    }

    /**
     * Add a link to the project.
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
                    .addIf("links")
                    .add("link")
                    .attr("rel", rel)
                    .attr("href", href)
            );
        }
    }

    /**
     * Remove a link from the project.
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
    }

    /**
     * Get all project links.
     * @param pid Project ID
     * @return Links found
     * @throws IOException If fails
     */
    public Collection<String> links(final String pid) throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item).nodes(
                String.format(
                    "/catalog/project[@id='%s']/links/link",
                    pid
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
     * Set a parent to the project.
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
                    .addIf("parent")
                    .set(parent)
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("catalog.xml");
    }

}
