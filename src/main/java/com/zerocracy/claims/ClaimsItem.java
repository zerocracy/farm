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
package com.zerocracy.claims;

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.cactoos.Proc;
import org.cactoos.collection.Filtered;
import org.cactoos.collection.Limited;
import org.cactoos.collection.Mapped;
import org.cactoos.collection.Sorted;
import org.cactoos.func.IoCheckedProc;
import org.cactoos.iterable.LengthOf;
import org.cactoos.map.MapOf;
import org.cactoos.time.DateAsText;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * Claims XML project's item.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ClaimsItem {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param project Project
     */
    public ClaimsItem(final Project project) {
        this.project = project;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public ClaimsItem bootstrap() throws IOException {
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
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void add(final Iterable<Directive> claim) throws IOException {
        try (final Item item = this.item()) {
            final List<XML> claims = new XMLDocument(
                new Xembler(
                    new Directives()
                        .add("claims")
                        .append(claim)
                ).dom()
            ).nodes("/claims/claim");
            final Xocument xocument = new Xocument(item);
            final List<XML> nodes = xocument.nodes("/claims/claim");
            final Set<String> signatures = new HashSet<>(
                new Sorted<>(
                    new Mapped<>(
                        xml -> ClaimsItem.signature(new ClaimIn(xml)),
                        nodes
                    )
                )
            );
            final Collection<Iterable<Directive>> filtered = new Mapped<>(
                entry -> new Directives().xpath("/claims").append(
                    new Directives()
                    .add("claim")
                    .append(Directives.copyOf(entry.getValue().node()))
                ),
                new Filtered<>(
                    entry -> !signatures.contains(entry.getKey()),
                    new MapOf<>(
                        xml -> ClaimsItem.signature(new ClaimIn(xml)),
                        xml -> xml,
                        claims
                    ).entrySet()
                )
            );
            for (final Iterable<Directive> dirs : filtered) {
                xocument.modify(dirs);
            }
            if (filtered.size() != claims.size()) {
                Logger.error(
                    this,
                    new Par(
                        "Duplicate claims are not allowed in %s,",
                        "can't add this XML to %d existing ones:\n%s"
                    ).say(
                        this.project.pid(),
                        nodes.size(),
                        new Xembler(claim).xmlQuietly()
                    )
                );
            }
        } catch (final ImpossibleModificationException err) {
            throw new IOException("Failed to read input claim", err);
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
     * @param proc The proc to run if taken
     * @return TRUE if something was taken
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public boolean take(final Proc<XML> proc) throws IOException {
        boolean taken = false;
        for (final XML xml : new Limited<>(1, this.iterate())) {
            try {
                new IoCheckedProc<>(proc).exec(xml);
            } finally {
                this.delete(xml);
            }
            taken = true;
        }
        return taken;
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
     * Delete one claim.
     * @param claim The XML
     * @throws IOException If fails
     */
    private void delete(final XML claim) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item).modify(
                new Directives().xpath(
                    String.format(
                        "/claims/claim[@id='%d' and type='%s']",
                        Long.parseLong(claim.xpath("@id").get(0)),
                        claim.xpath("type/text()").get(0)
                    )
                ).strict(1).remove()
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

    /**
     * Claim signature.
     *
     * @param cin Claim in
     * @return Signature
     */
    private static String signature(final ClaimIn cin) {
        final String token;
        if (cin.hasToken()) {
            token = cin.token();
        } else {
            token = "";
        }
        return String.format(
            "%s;%s;%s",
            cin.type(),
            cin.params(),
            token
        );
    }
}
