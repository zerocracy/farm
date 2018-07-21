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
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.Project;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Func;
import org.cactoos.Input;
import org.cactoos.cache.SoftFunc;
import org.cactoos.func.SyncFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.InputOf;
import org.cactoos.io.StickyInput;
import org.cactoos.io.SyncInput;
import org.cactoos.iterable.LengthOf;
import org.cactoos.scalar.And;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.JoinedText;

/**
 * Ruled rules.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RdRules {

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
     * The reason for validation.
     */
    private final String reason;

    /**
     * Ctor.
     * @param pkt Project
     * @param file File with item
     * @param rsn Reason
     */
    RdRules(final Project pkt, final Path file, final String rsn) {
        this.project = pkt;
        this.path = file;
        this.reason = rsn;
    }

    /**
     * Validate document.
     * @throws IOException If fails
     */
    public void validate() throws IOException {
        final String area = new RdArea(this.path).value();
        final Iterable<String> xsls = new RdIndex(
            URI.create(
                String.format(
                    "/latest/rules/%s",
                    StringUtils.substringBefore(
                        area, "/"
                    )
                )
            )
        ).iterate();
        new UncheckedScalar<>(new And(this::check, xsls)).value();
        if (Logger.isDebugEnabled(this)) {
            Logger.debug(
                // @checkstyle LineLength (1 line)
                this, "%d XSLs confirm consistency in %s after changes in %s, area=\"%s\", reason=\"%s\", XSLs=\"%s\"",
                new LengthOf(xsls).intValue(), this.project,
                this.path.getFileName(), area, this.reason,
                new JoinedText(";", xsls).asString()
            );
        }
    }

    /**
     * Check for consistency.
     * @param xsl The URI of the XSL that modifies
     * @throws IOException If fails
     */
    private void check(final String xsl) throws IOException {
        final Collection<String> errors =
            XSLDocument.make(RdRules.CACHE.apply(URI.create(xsl)).stream())
                .with(new RdSources(this.project))
                .transform(new XMLDocument("<i/>"))
                .xpath("/errors/error/text()");
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                String.format(
                    "%s says: %s",
                    xsl, String.join("; ", errors)
                )
            );
        }
    }

}
