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
package com.zerocracy.pm;

import com.jcabi.xml.XML;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Claims.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
public final class Claims implements Closeable {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Item.
     */
    private final AtomicReference<Item> item;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Claims(final Project pkt) {
        this.project = pkt;
        this.item = new AtomicReference<>();
    }

    /**
     * Bootstrap it and lock.
     * @return Itself
     * @throws IOException If fails
     */
    public Claims lock() throws IOException {
        this.item.set(this.project.acq("claims.xml"));
        new Xocument(this.item.get().path()).bootstrap("pm/claims");
        return this;
    }

    @Override
    public void close() throws IOException {
        this.item.get().close();
        this.item.set(null);
    }

    /**
     * Add new claim.
     * @param dirs Directives
     * @throws IOException If fails
     */
    public void add(final Iterable<Directive> dirs) throws IOException {
        new Xocument(this.item.get().path()).modify(
            new Directives()
                .xpath("/claims").add("claim")
                .attr("id", System.nanoTime() % (long) Integer.MAX_VALUE)
                .add("created")
                .set(
                    ZonedDateTime.now().format(
                        DateTimeFormatter.ISO_INSTANT
                    )
                )
                .up()
                .append(dirs)
        );
    }

    /**
     * Remove claim.
     * @param cid Claim ID
     * @throws IOException If fails
     */
    public void remove(final String cid) throws IOException {
        new Xocument(this.item.get().path()).modify(
            new Directives().xpath(
                String.format(
                    "/claims/claim[@id='%s']", cid
                )
            ).remove()
        );
    }

    /**
     * Find claims by XPath term.
     * @param term Search term
     * @return List of found claims
     * @throws IOException If fails
     */
    public Collection<XML> find(final String term) throws IOException {
        String xpath = term;
        if (!xpath.isEmpty()) {
            xpath = String.format("[%s]", xpath);
        }
        return new Xocument(this.item.get().path()).nodes(
            String.format("/claims/claim%s", xpath)
        );
    }

}
