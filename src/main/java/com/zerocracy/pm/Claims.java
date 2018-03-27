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
package com.zerocracy.pm;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import org.cactoos.collection.Limited;
import org.cactoos.collection.Mapped;
import org.cactoos.collection.Sorted;
import org.cactoos.iterable.LengthOf;
import org.cactoos.time.DateAsText;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Claims.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
     * @param claim The claim to add
     * @throws IOException If fails
     */
    public void add(final XML claim) throws IOException {
        this.add(Directives.copyOf(claim.node()));
    }

    /**
     * Add new directives.
     * @param claim The claim to add
     * @throws IOException If fails
     */
    public void add(final Iterable<Directive> claim) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives().xpath("/claims").append(claim)
            );
            final Collection<String> signatures = new Sorted<>(
                new Mapped<>(
                    input -> {
                        final ClaimIn cin = new ClaimIn(input);
                        return String.format(
                            "%s;%s",
                            cin.type(),
                            cin.params()
                        );
                    },
                    new Xocument(item).nodes("/claims/claim")
                )
            );
            if (signatures.size() != new HashSet<>(signatures).size()) {
                throw new IllegalStateException(
                    new Par(
                        "Duplicate claims are not allowed in %s,",
                        "can't add this XML:\n%s"
                    ).say(this.project.pid(), new Xembler(claim).xmlQuietly())
                );
            }
        }
        final int size = new LengthOf(this.iterate()).intValue();
        if (size > Tv.HUNDRED) {
            throw new IllegalStateException(
                String.format(
                    "Can't add, claims overflow in %s, too many items: %d",
                    this.project.pid(), size
                )
            );
        }
    }

    /**
     * Take one claim and remove it.
     * @return Found (or empty)
     * @throws IOException If fails
     */
    public Iterator<XML> take() throws IOException {
        try (final Item item = this.item()) {
            final Iterable<XML> found = new Limited<>(1, this.iterate());
            if (found.iterator().hasNext()) {
                final XML next = found.iterator().next();
                new Xocument(item).modify(
                    new Directives().xpath(
                        String.format(
                            "/claims/claim[@id='%d' and type='%s']",
                            Long.parseLong(next.xpath("@id").get(0)),
                            next.xpath("type/text()").get(0)
                        )
                    ).strict(1).remove()
                );
            }
            return found.iterator();
        }
    }

    /**
     * Iterate them all.
     * @return List of all claims
     * @throws IOException If fails
     */
    public Collection<XML> iterate() throws IOException {
        final String now = new DateAsText().asString();
        try (final Item item = this.item()) {
            return new Sorted<>(
                new Comparator<XML>() {
                    @Override
                    public int compare(final XML left, final XML right) {
                        return Long.compare(this.cid(left), this.cid(right));
                    }
                    private long cid(final XML xml) {
                        return new ClaimIn(xml).cid();
                    }
                },
                new Xocument(item).nodes(
                    String.format(
                        "/claims/claim[not(until) or until < '%s']", now
                    )
                )
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
