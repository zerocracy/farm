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
package com.zerocracy.farm.ruled;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Func;
import org.cactoos.Input;
import org.cactoos.cache.SoftFunc;
import org.cactoos.func.SyncFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.InputOf;
import org.cactoos.io.LengthOf;
import org.cactoos.io.StickyInput;
import org.cactoos.io.SyncInput;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.And;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.TextOf;

/**
 * Auto updater of XML documents.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RdAuto {

    /**
     * Cache of documents.
     */
    private static final UncheckedFunc<URI, Input> CACHE = new UncheckedFunc<>(
        new SyncFunc<>(
            new SoftFunc<>(
                (Func<URI, Input>) uri -> new SyncInput(
                    new StickyInput(new InputOf(uri))
                )
            )
        )
    );

    /**
     * Original project.
     */
    private final Project project;

    /**
     * The file.
     */
    private final Path path;

    /**
     * The reason.
     */
    private final String reason;

    /**
     * Ctor.
     * @param pkt Project
     * @param file The file with the item to start with
     * @param rsn The reason
     */
    RdAuto(final Project pkt, final Path file, final String rsn) {
        this.project = pkt;
        this.path = file;
        this.reason = rsn;
    }

    /**
     * Propagate changes to other documents.
     * @throws IOException If fails
     */
    public void propagate() throws IOException {
        new UncheckedScalar<>(
            new And(
                this::auto,
                new RdIndex(
                    URI.create(
                        String.format(
                            "/latest/auto/%s",
                            new RdArea(this.path).value()
                        )
                    )
                ).iterate()
            )
        ).value();
    }

    /**
     * Auto-modify one document.
     * @param xsl The URI of the XSL that modifies
     * @throws IOException If fails
     */
    private void auto(final String xsl) throws IOException {
        final String target = String.format(
            "%s.xml",
            StringUtils.substringBefore(
                StringUtils.substringAfter(
                    StringUtils.substringAfterLast(xsl, "/"), "-"
                ), "-"
            )
        );
        try (final Item item = this.project.acq(target)) {
            if (item.path().toFile().exists()
                && item.path().toFile().length() > 0L) {
                final XML xml = new XMLDocument(item.path().toFile());
                final XML after = new XSLDocument(
                    new TextOf(
                        RdAuto.CACHE.apply(URI.create(xsl))
                    ).asString(),
                    new RdSources(this.project)
                ).transform(xml);
                if (!xml.equals(after)) {
                    new LengthOf(
                        new TeeInput(
                            after.toString(),
                            item.path()
                        )
                    ).intValue();
                    Logger.info(
                        this, "Applied %s to %s in %s (%d to %d): %s",
                        xsl, target, this.project,
                        xml.toString().length(), after.toString().length(),
                        this.reason
                    );
                }
            }
        }
    }

}
