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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.func.Ternary;
import org.cactoos.func.UncheckedScalar;
import org.cactoos.io.InputAsBytes;
import org.cactoos.io.InputWithFallback;
import org.cactoos.io.LengthOfInput;
import org.cactoos.io.PathAsInput;
import org.cactoos.io.PathAsOutput;
import org.cactoos.io.TeeInput;
import org.cactoos.io.UrlAsInput;
import org.cactoos.list.ReducedIterable;
import org.cactoos.list.ReverseIterable;
import org.cactoos.list.StickyList;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.SplitText;
import org.cactoos.text.UncheckedText;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * XML document.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class Xocument {

    /**
     * Current DATUM version.
     */
    private static final String VERSION = "0.26";

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
            new BytesAsText(
                new InputAsBytes(
                    new PathAsInput(this.file)
                )
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
        );
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
            new LengthOfInput(
                new TeeInput(after, new PathAsOutput(this.file))
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
                new ReducedIterable<>(
                    new ReverseIterable<>(
                        new StickyList<>(
                            new SplitText(
                                new BytesAsText(
                                    new InputWithFallback(
                                        new UrlAsInput(
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
                        )
                    ),
                    xml,
                    (input, line) -> {
                        XML output = input;
                        final String[] parts = line.split(" ");
                        if (Xocument.compare(parts[0], version) > 0) {
                            output = XSLDocument.make(
                                new UrlAsInput(
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
            new LengthOfInput(
                new TeeInput(after.toString(), new PathAsOutput(this.file))
            ).value();
        }
        return after;
    }

    /**
     * Build URL.
     * @param path Path
     * @return URL
     */
    private static String url(final String path) {
        return String.format(
            "http://datum.zerocracy.com/%s",
            path
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
