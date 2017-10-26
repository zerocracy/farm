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

import com.jcabi.log.Logger;
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
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.func.StickyFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputStreamOf;
import org.cactoos.io.InputWithFallback;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.iterable.Reduced;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.UncheckedScalar;
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
    public static final String VERSION = "0.43.1";

    /**
     * Cache of documents.
     */
    private static final UncheckedFunc<URL, XML> INDEXES = new UncheckedFunc<>(
        new StickyFunc<>(
            (Func<URL, XML>) url -> new XMLDocument(
                new TextOf(
                    new InputWithFallback(
                        new InputOf(url),
                        new InputOf("<index/>")
                    )
                ).asString()
            )
        )
    );

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
    private final UncheckedScalar<Path> file;

    /**
     * Ctor.
     * @param item Item
     */
    public Xocument(final Item item) {
        this(item::path);
    }

    /**
     * Ctor.
     * @param path File
     */
    public Xocument(final Path path) {
        this(() -> path);
    }

    /**
     * Ctor.
     * @param path File
     */
    private Xocument(final Scalar<Path> path) {
        this.file = new UncheckedScalar<>(path);
    }

    @Override
    public String toString() {
        return new UncheckedText(
            new TextOf(
                new InputOf(this.file.value())
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
        final Path path = this.file.value();
        if (!Files.exists(path) || Files.size(path) == 0L) {
            Files.write(
                path,
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
        final XML xml = this.upgraded(new XMLDocument(path.toFile()), xsd);
        final String schema = xml.xpath(
            String.format("/%s/@xsi:noNamespaceSchemaLocation", root)
        ).get(0);
        if (!schema.equals(uri)) {
            this.modify(
                new Directives().xpath(String.format("/%s", root)).attr(
                    "xsi:noNamespaceSchemaLocation", uri
                )
            );
            Logger.info(
                this, "XSD upgraded to \"%s\" in %s", uri,
                this.file.value().getFileName()
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
            new XMLDocument(this.file.value().toFile()),
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
            new XMLDocument(this.file.value().toFile()),
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
        final XML xml = new StrictXML(
            Xocument.COMPRESS.with(
                "version", Xocument.VERSION
            ).transform(new XMLDocument(node)),
            Xocument.RESOLVER
        );
        final String after = xml.toString();
        if (!before.toString().equals(after)) {
            new LengthOf(new TeeInput(after, this.file.value())).value();
        }
    }

    /**
     * Upgrade if necessary.
     * @param xml XML to upgrade
     * @param xsd Path to XSD, eg "pm/scope/wbs"
     * @return Upgraded
     * @throws IOException If fails
     */
    private XML upgraded(final XML xml, final String xsd) throws IOException {
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
                    Xocument.INDEXES.apply(
                        Xocument.url(
                            String.format(
                                "/latest/upgrades/%s/index.xml",
                                xsd
                            )
                        )
                    ).nodes("/index/entry[@dir='false']"),
                    xml,
                    (input, node) -> {
                        XML output = input;
                        final String ver = node.xpath("@order").get(0);
                        if (Xocument.compare(ver, version) > 0
                            && Xocument.compare(ver, Xocument.VERSION) < 0) {
                            final URL url = new URL(node.xpath("@uri").get(0));
                            output = XSLDocument.make(
                                new InputStreamOf(url)
                            ).transform(input);
                            Logger.info(
                                this,
                                "XML %s.xml upgraded to \"%s\" by %s in %s",
                                xsd, ver, url, this.file.value().getFileName()
                            );
                        }
                        return output;
                    }
                )
            ).value();
            new LengthOf(
                new TeeInput(after.toString(), this.file.value())
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
     * Version as a cid.
     * @param ver Version
     * @return The cid
     */
    private static int num(final String ver) {
        final List<String> parts = new LinkedList<>();
        parts.addAll(new ListOf<>(ver.split("\\.")));
        // @checkstyle MagicNumber (1 line)
        if (parts.size() < 3) {
            parts.add("0");
        }
        int sum = 0;
        for (int idx = parts.size() - 1; idx >= 0; --idx) {
            sum += Integer.parseInt(parts.get(idx))
                // @checkstyle MagicNumber (1 line)
                << (parts.size() - idx << 3);
        }
        return sum;
    }

}
