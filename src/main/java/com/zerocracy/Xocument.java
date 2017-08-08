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
package com.zerocracy;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.jstk.Item;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputWithFallback;
import org.cactoos.io.LengthOf;
import org.cactoos.io.OutputTo;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.Reduced;
import org.cactoos.iterable.StickyList;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.UncheckedScalar;
import org.cactoos.text.SplitText;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * XML document.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 * @since 0.1
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class Xocument {

    /**
     * Current DATUM version.
     */
    private static final String VERSION = "0.34";

    /**
     * Compressing XSL.
     */
    private static final XSL COMPRESS = XSLDocument.make(
        Xocument.class.getResource("compress.xsl")
    );

    /**
     * XSD resolver.
     */
    private static final LSResourceResolver RESOLVER = new XsdResolver();

    /**
     * File.
     */
    private final Path file;

    /**
     * Ctor.
     * @param item Item
     * @throws IOException If fails
     */
    public Xocument(final Item item) throws IOException {
        this(item.path());
    }

    /**
     * Ctor.
     * @param path File
     */
    public Xocument(final Path path) {
        this.file = path;
    }

    @Override
    public String toString() {
        return new UncheckedText(
            new TextOf(
                new InputOf(this.file)
            )
        ).asString();
    }

    /**
     * Bootstrap it.
     * @param xsd Path of XSD
     * @return This
     * @throws IOException If fails
     */
    public Xocument bootstrap(final String xsd)
        throws IOException {
        final String root = StringUtils.substringAfterLast(xsd, "/");
        final String uri = Xocument.url(
            String.format("/%s/xsd/%s.xsd", Xocument.VERSION, xsd)
        ).toString();
        if (!Files.exists(this.file) || Files.size(this.file) == 0L) {
            Files.write(
                this.file,
                String.join(
                    " ",
                    String.format("<%s", root),
                    String.format("version='%s'", Xocument.VERSION),
                    String.format(
                        "updated='%s'",
                        ZonedDateTime.now().format(
                            DateTimeFormatter.ISO_INSTANT
                        )
                    ),
                    "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'",
                    String.format(
                        "xsi:noNamespaceSchemaLocation='%s'/>", uri
                    )
                ).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE
            );
        }
        final XML xml = this.upgraded(new XMLDocument(this.file.toFile()), xsd);
        final String schema = xml.xpath(
            String.format("/%s/@xsi:noNamespaceSchemaLocation", root)
        ).get(0);
        if (!schema.equals(uri)) {
            this.modify(
                new Directives().xpath(String.format("/%s", root)).attr(
                    "xsi:noNamespaceSchemaLocation", uri
                )
            );
        }
        return this;
    }

    /**
     * Query it.
     * @param xpath Query string
     * @return Found texts
     * @throws IOException If fails
     */
    public List<String> xpath(final String xpath) throws IOException {
        final XML xml = new StrictXML(
            new XMLDocument(this.file.toFile()),
            Xocument.RESOLVER
        );
        return xml.xpath(xpath);
    }

    /**
     * Query it.
     * @param xpath Query string
     * @return Found nodes
     * @throws IOException If fails
     */
    public List<XML> nodes(final String xpath) throws IOException {
        final XML xml = new StrictXML(
            new XMLDocument(this.file.toFile()),
            Xocument.RESOLVER
        );
        return xml.nodes(xpath);
    }

    /**
     * Modify it.
     * @param dirs Directives
     * @throws IOException If fails
     */
    public void modify(final Iterable<Directive> dirs) throws IOException {
        final XML before = new XMLDocument(this.toString());
        final Node node = before.node();
        new Xembler(dirs).applyQuietly(node);
        final String after = new StrictXML(
            Xocument.COMPRESS.with(
                "version", Xocument.VERSION
            ).transform(new XMLDocument(node)),
            Xocument.RESOLVER
        ).toString();
        if (!before.toString().equals(after)) {
            new LengthOf(
                new TeeInput(after, new OutputTo(this.file))
            ).value();
        }
    }

    /**
     * Upgrade if necessary.
     * @param xml XML to upgrade
     * @param xsd Path to XSD, eg "pm/scope/wbs"
     * @return Upgraded
     * @throws IOException If fails
     */
    public XML upgraded(final XML xml, final String xsd) throws IOException {
        final String version = new UncheckedScalar<>(
            new Ternary<String>(
                xml.xpath("/*/@version"),
                List::isEmpty,
                xpath -> "0.0",
                xpath -> xpath.get(0)
            )
        ).value();
        final XML after;
        if (version.equals(Xocument.VERSION)) {
            after = xml;
        } else {
            after = new UncheckedScalar<>(
                new Reduced<>(
                    new StickyList<>(
                        new SplitText(
                            new TextOf(
                                new InputWithFallback(
                                    new InputOf(
                                        Xocument.url(
                                            String.format(
                                                "/latest/upgrades/%s/list",
                                                xsd
                                            )
                                        )
                                    )
                                )
                            ),
                            "\n"
                        )
                    ),
                    xml,
                    (input, line) -> {
                        XML output = input;
                        final String[] parts = line.split(" ");
                        if (Xocument.compare(parts[0], version) > 0) {
                            output = XSLDocument.make(
                                new InputOf(
                                    Xocument.url(
                                        String.format(
                                            "/latest/%s",
                                            parts[1]
                                        )
                                    )
                                ).stream()
                            ).transform(input);
                        }
                        return output;
                    }
                )
            ).value();
            new LengthOf(
                new TeeInput(after.toString(), new OutputTo(this.file))
            ).value();
        }
        return after;
    }

    /**
     * Build URL.
     * @param path Path
     * @return URL
     * @throws MalformedURLException If invalid path
     */
    private static URL url(final String path) throws MalformedURLException {
        return new URL(
            String.format(
                "http://datum.zerocracy.com%s",
                path
            )
        );
    }

    /**
     * Compare two versions.
     * @param left Left version
     * @param right Right version
     * @return Result (>0 if left is bigger than right)
     */
    private static int compare(final String left, final String right) {
        return Integer.compare(Xocument.num(left), Xocument.num(right));
    }

    /**
     * Version as a number.
     * @param ver Version
     * @return The number
     */
    private static int num(final String ver) {
        final String[] parts = ver.split("\\.");
        int sum = 0;
        for (int idx = parts.length - 1; idx >= 0; --idx) {
            sum += Integer.parseInt(parts[idx]) << (idx << Tv.THREE);
        }
        return sum;
    }

}
