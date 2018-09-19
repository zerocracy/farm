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
package com.zerocracy.pm.staff;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.UncheckedScalar;
import org.w3c.dom.Node;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Election data.
 *
 * @since 1.0
 */
public final class Election implements XML {

    /**
     * Origin XML.
     */
    private final UncheckedScalar<XML> origin;

    /**
     * Ctor.
     *
     * @param job Job
     * @param logins User logins
     * @param voters Voters
     */
    public Election(final String job, final Iterable<String> logins,
        final Map<Votes, Integer> voters) {
        this(
            new Directives()
                .add("election")
                .attr("date", Instant.now().toString())
                .append(new Election.XeVotes(job, logins, voters))
        );
    }

    /**
     * Primary ctor.
     *
     * @param dirs XML directives
     */
    private Election(final Iterable<Directive> dirs) {
        this.origin = new UncheckedScalar<>(
            new StickyScalar<>(
                () -> new XMLDocument(new Xembler(dirs).dom())
            )
        );
    }

    @Override
    public List<String> xpath(final String query) {
        return this.origin.value().xpath(query);
    }

    @Override
    public List<XML> nodes(final String query) {
        return this.origin.value().nodes(query);
    }

    @Override
    public XML registerNs(final String prefix, final Object uri) {
        return this.origin.value().registerNs(prefix, uri);
    }

    @Override
    public XML merge(final NamespaceContext context) {
        return this.origin.value().merge(context);
    }

    @Override
    public Node node() {
        return this.origin.value().node();
    }

    /**
     * Xembler votes.
     */
    private static final class XeVotes implements Iterable<Directive> {

        /**
         * Job id.
         */
        private final String job;

        /**
         * User logins.
         */
        private final Iterable<String> logins;

        /**
         * Voters.
         */
        private final Map<Votes, Integer> voters;

        /**
         * Ctor.
         *
         * @param job Job id
         * @param logins User logins
         * @param voters Voters
         */
        XeVotes(final String job, final Iterable<String> logins,
            final Map<Votes, Integer> voters) {
            this.job = job;
            this.logins = logins;
            this.voters = voters;
        }

        @Override
        public Iterator<Directive> iterator() {
            final Directives dirs = new Directives();
            final StringBuilder log = new StringBuilder(0);
            for (final Map.Entry<Votes, Integer> ent
                : this.voters.entrySet()) {
                dirs.add("vote")
                    .attr("author", ent.getKey().toString())
                    .attr("weight", ent.getValue());
                for (final String login : this.logins) {
                    log.setLength(0);
                    try {
                        dirs.add("person")
                            .attr("login", login)
                            .attr("points", ent.getKey().take(login, log))
                            .set(log.toString())
                            .up();
                    } catch (final IOException err) {
                        throw new IllegalStateException("Failed to vote", err);
                    }
                }
                dirs.up();
            }
            return dirs.iterator();
        }
    }
}
