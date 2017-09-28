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
package com.zerocracy.pm;

import com.jcabi.xml.XML;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import org.cactoos.collection.Sorted;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Claims.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 */
public final class Claims {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Claims(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Claims bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).bootstrap("pm/claims");
        }
        return this;
    }

    /**
     * Add new directives.
     * @param dirs Directives
     * @throws IOException If fails
     */
    public void add(final Iterable<Directive> dirs) throws IOException {
        if (!dirs.iterator().hasNext()) {
            throw new IllegalArgumentException("Empty directives");
        }
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives().xpath("/claims").append(dirs)
            );
        }
    }

    /**
     * Remove claim.
     * @param cid Claim ID
     * @throws IOException If fails
     */
    public void remove(final long cid) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives().xpath(
                    String.format(
                        "/claims/claim[@id='%d']", cid
                    )
                ).strict(1).remove()
            );
        }
    }

    /**
     * Iterate them all.
     * @return List of all claims
     * @throws IOException If fails
     */
    public Collection<XML> iterate() throws IOException {
        try (final Item item = this.item()) {
            return new Sorted<>(
                new Comparator<XML>() {
                    @Override
                    public int compare(final XML left, final XML right) {
                        return Long.compare(this.cid(left), this.cid(right));
                    }
                    private long cid(final XML xml) {
                        return new ClaimIn(xml).number();
                    }
                },
                new Xocument(item).nodes("/claims/claim")
            );
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("claims.xml");
    }

}
