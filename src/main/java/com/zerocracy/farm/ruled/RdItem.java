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
package com.zerocracy.farm.ruled;

import com.jcabi.log.Logger;
import com.jcabi.xml.Sources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputWithFallback;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.And;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.TextOf;

/**
 * Ruled item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "origin")
final class RdItem implements Item, Sources {

    /**
     * Original project.
     */
    private final Project project;

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * The location of the file.
     */
    private final AtomicReference<Path> file;

    /**
     * The length of the file.
     */
    private long length;

    /**
     * Ctor.
     * @param pkt Project
     * @param item Original item
     */
    RdItem(final Project pkt, final Item item) {
        this.project = pkt;
        this.origin = item;
        this.file = new AtomicReference<>();
        this.length = 0L;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        final Path path = this.origin.path();
        this.file.set(path);
        if (Files.exists(path) && path.toFile().length() > 0L) {
            this.length = path.toFile().length();
//            this.validate(RdItem.area(path));
        }
        return path;
    }

    @Override
    public void close() throws IOException {
        final Path path = this.file.get();
        final boolean modified = Files.exists(path)
            && this.length != path.toFile().length();
        if (modified) {
            final String area = RdItem.area(path);
            this.origin.close();
            this.propagate(area);
            this.validate(area);
        } else {
            this.origin.close();
        }
    }

    @Override
    public Source resolve(final String href, final String base)
        throws TransformerException {
        try (final Item item = this.project.acq(href)) {
            final Path path = item.path();
            final Input input;
            if (path.toFile().length() > 0L) {
                input = new InputOf(item.path());
            } else {
                input = new InputOf("<always-empty/>");
            }
            return new StreamSource(input.stream());
        } catch (final IOException ex) {
            throw new TransformerException(ex);
        }
    }

    /**
     * Get are of the path.
     * @return The area, e.g. "pm/scope/wbs"
     * @throws IOException If fails
     */
    public static String area(final Path path) throws IOException {
        return StringUtils.substringBeforeLast(
            StringUtils.substringAfter(
                new XMLDocument(
                    path.toFile()
                ).xpath("/*/@xsi:noNamespaceSchemaLocation").get(0),
                "/xsd/"
            ),
            ".xsd"
        );
    }

    /**
     * Propagate changes to other documents.
     * @param area XSD location, e.g. "pm/scope/wbs"
     * @throws IOException If fails
     */
    private void validate(final String area) throws IOException {
        new UncheckedScalar<>(
            new And(
                RdItem.index(
                    String.format(
                        "/latest/rules/%s",
                        StringUtils.substringBefore(area, "/")
                    )
                ),
                this::check
            )
        ).value();
    }

    /**
     * Propagate changes to other documents.
     * @param area XSD location, e.g. "pm/scope/wbs"
     * @throws IOException If fails
     */
    private void propagate(final String area) throws IOException {
        new UncheckedScalar<>(
            new And(
                RdItem.index(
                    String.format(
                        "/latest/auto/%s", area
                    )
                ),
                this::auto
            )
        ).value();
    }

    /**
     * Get entries from the index at the URI.
     * @param uri URI of the index (path only)
     * @return List of URIs
     * @throws IOException If fails
     */
    private static Iterable<String> index(final String uri) throws IOException {
        return new XMLDocument(
            new TextOf(
                new InputWithFallback(
                    new InputOf(
                        URI.create(
                            String.format(
                                "http://datum.zerocracy.com%s/index.xml",
                                uri
                            )
                        )
                    ),
                    new InputOf("<index/>")
                )
            ).asString()
        ).xpath("/index/entry[@dir='false']/@uri");
    }

    /**
     * Check for consistency.
     * @param xsl The URI of the XSL that modifies
     * @throws IOException If fails
     */
    private void check(final String xsl) throws IOException {
        final Collection<String> errors =
            XSLDocument.make(new InputOf(URI.create(xsl)).stream())
                .with(this)
                .transform(new XMLDocument("<i/>"))
                .xpath("/errors/error/text()");
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                String.join("; ", errors)
            );
        }
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
            if (Files.exists(item.path())
                && item.path().toFile().length() > 0L) {
                final XML xml = new XMLDocument(item.path().toFile());
                new LengthOf(
                    new TeeInput(
                        XSLDocument.make(
                            new InputOf(
                                URI.create(xsl)
                            ).stream()
                        ).with(this).transform(xml).toString(),
                        item.path()
                    )
                ).value();
                Logger.info(
                    this, "Applied %s to %s in %s",
                    xsl, target, this.project
                );
            }
        }
    }

}
