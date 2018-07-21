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
package com.zerocracy.farm.guts;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cactoos.Scalar;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Guts.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class GsProject implements Project {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Query.
     */
    private final String query;

    /**
     * Dirs to add.
     */
    private final IoCheckedScalar<Iterable<Directive>> dirs;

    /**
     * Ctor.
     * @param frm Farm
     * @param qry The query
     * @param anex Dirs to add
     */
    GsProject(final Farm frm, final String qry,
        final Scalar<Iterable<Directive>> anex) {
        this(frm, qry, new IoCheckedScalar<>(anex));
    }

    /**
     * Ctor.
     * @param frm Farm
     * @param qry The query
     * @param anex Dirs to add
     */
    GsProject(final Farm frm, final String qry,
        final IoCheckedScalar<Iterable<Directive>> anex) {
        this.farm = frm;
        this.query = qry;
        this.dirs = anex;
    }

    @Override
    public String pid() {
        return "";
    }

    @Override
    public Item acq(final String file) throws IOException {
        final Path temp = Files.createTempFile("farm", ".xml");
        final Iterator<Project> pkts = this.farm.find(this.query).iterator();
        XML before = new XMLDocument(
            new Xembler(GsProject.start()).xmlQuietly()
        );
        if (pkts.hasNext()) {
            try (final Item item = pkts.next().acq(file)) {
                final Path path = item.path();
                if (path.toFile().exists()
                    && path.toFile().length() != 0L) {
                    before = new XMLDocument(path);
                }
            }
        }
        new LengthOf(
            new TeeInput(
                new XMLDocument(
                    new Xembler(this.dirs.value()).applyQuietly(
                        before.node()
                    )
                ).toString(),
                temp
            )
        ).intValue();
        return new FkItem(temp);
    }

    /**
     * Start XML.
     * @return Dirs
     */
    private static Iterable<Directive> start() {
        final Map<String, Object> attrs = new HashMap<>(0);
        attrs.put(
            "availableProcessors",
            Runtime.getRuntime().availableProcessors()
        );
        attrs.put(
            "freeMemory",
            Runtime.getRuntime().freeMemory()
        );
        attrs.put(
            "maxMemory",
            Runtime.getRuntime().maxMemory()
        );
        attrs.put(
            "totalMemory",
            Runtime.getRuntime().totalMemory()
        );
        attrs.put(
            "totalThreads",
            Thread.getAllStackTraces().size()
        );
        return new Directives()
            .pi("xml-stylesheet", "href='/xsl/guts.xsl' type='text/xsl'")
            .add("guts")
            .add("jvm")
            .add("attrs")
            .append(
                new Joined<>(
                    new Mapped<Map.Entry<String, Object>, Iterable<Directive>>(
                        ent -> new Directives().add("attr")
                            .attr("id", ent.getKey())
                            .set(ent.getValue()).up(),
                        attrs.entrySet()
                    )
                )
            )
            .up()
            .add("threads")
            .append(
                new Joined<>(
                    new Mapped<Thread, Iterable<Directive>>(
                        thread -> new Directives()
                            .add("thread")
                            .attr("id", thread.getName())
                            .attr("state", thread.getState())
                            .attr("daemon", thread.isDaemon())
                            .attr("alive", thread.isAlive())
                            .up(),
                        Thread.getAllStackTraces().keySet()
                    )
                )
            )
            .up()
            .up();
    }

}
