/**
 * Copyright (c) 2016 Zerocracy
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

import com.jcabi.xml.StrictXML;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.zerocracy.jstk.Item;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import org.w3c.dom.Node;
import org.xembly.Directive;
import org.xembly.Xembler;

/**
 * XML document.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Xocument {

    /**
     * Current DATUM version.
     */
    private static final String VERSION = "0.4";

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

    /**
     * Bootstrap it.
     * @param root Root node name
     * @param xsd Path of XSD
     * @throws IOException If fails
     */
    public void bootstrap(final String root, final String xsd)
        throws IOException {
        if (!Files.exists(this.file) || Files.size(this.file) == 0L) {
            Files.write(
                this.file,
                String.format(
                    // @checkstyle LineLength (1 line)
                    "<%s xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='https://raw.githubusercontent.com/zerocracy/datum/%s/xsd/%s.xsd'/>",
                    root, Xocument.VERSION, xsd
                ).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE
            );
        }
    }

    /**
     * Query it.
     * @param xpath Query string
     * @return Found texts
     * @throws IOException If fails
     */
    public Collection<String> xpath(final String xpath) throws IOException {
        final XML xml = new StrictXML(new XMLDocument(this.file.toFile()));
        return xml.xpath(xpath);
    }

    /**
     * Modify it.
     * @param dirs Directives
     * @throws IOException If fails
     */
    public void modify(final Iterable<Directive> dirs) throws IOException {
        final Node node = new XMLDocument(this.file.toFile()).node();
        new Xembler(dirs).applyQuietly(node);
        Files.write(
            this.file,
            new StrictXML(new XMLDocument(node))
                .toString()
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Apply XSL to it.
     * @param xsl XSL to apply
     * @throws IOException If fails
     */
    public void apply(final XSL xsl) throws IOException {
        Files.write(
            this.file,
            new StrictXML(
                xsl.transform(new XMLDocument(this.file.toFile()))
            ).toString().getBytes(StandardCharsets.UTF_8)
        );
    }

}
