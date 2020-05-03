/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.jcabi.xml.XML;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.cactoos.Func;
import org.cactoos.Proc;
import org.xembly.Directive;

/**
 * XML item with {@link Xocument} inside.
 * @since 1.0
 */
public final class ItemXml {

    /**
     * Origin item.
     */
    private final Item origin;

    /**
     * XSD path.
     */
    private final String xsd;

    /**
     * Ctor.
     * @param origin Origin item
     */
    public ItemXml(final Item origin) {
        this(origin, "");
    }

    /**
     * Bootstrap item with XSD.
     * @param origin Origin item
     * @param xsd XSD path
     */
    public ItemXml(final Item origin, final String xsd) {
        this.origin = origin;
        this.xsd = xsd;
    }

    /**
     * Read {@link Xocument}.
     * @param reader Function to read
     * @param <T> Result type
     * @return Result
     * @throws IOException On failure
     */
    public <T> T read(final Func<Xocument, T> reader) throws IOException {
        return this.origin.read(
            path -> reader.apply(this.xocument(path))
        );
    }

    /**
     * Update with bootstrap.
     * @throws IOException On failure
     */
    public void update() throws IOException {
        this.origin.update(this::xocument);
    }

    /**
     * Update {@link Xocument}.
     * @param writer Function to update
     * @throws IOException On failure
     */
    public void update(final Proc<Xocument> writer) throws IOException {
        this.origin.update(
            path -> writer.exec(this.xocument(path))
        );
    }

    /**
     * Update {@link Xocument} with {@link org.xembly.Directives}.
     * @param dirs Directives
     * @throws IOException On failure
     */
    public void update(final Iterable<Directive> dirs) throws IOException {
        this.update(xoc -> xoc.modify(dirs));
    }

    /**
     * Read Xpath.
     * @param query Xpath query
     * @return List of strings
     * @throws IOException On failure
     */
    public List<String> xpath(final String query) throws IOException {
        return this.read(xoc -> xoc.xpath(query));
    }

    /**
     * Read Xpath with default value.
     * @param query Xpath query
     * @param def Default value if not found
     * @return Single string or default
     * @throws IOException On failure
     */
    public String xpath(final String query, final String def)
        throws IOException {
        return this.read(xoc -> xoc.xpath(query, def));
    }

    /**
     * Read nodes by Xpath.
     * @param query Xpath query
     * @return List of XML nodes
     * @throws IOException On failure
     */
    public List<XML> nodes(final String query) throws IOException {
        return this.read(xoc -> xoc.nodes(query));
    }

    /**
     * Check if empty.
     * @param query Xpath query
     * @return True if no nodes found
     * @throws IOException On failure
     */
    public boolean empty(final String query) throws IOException {
        return this.nodes(query).isEmpty();
    }

    /**
     * Check if exist.
     * @param query Xpath query
     * @return True if any node found
     * @throws IOException On failure
     */
    public boolean exists(final String query) throws IOException {
        return !this.empty(query);
    }

    /**
     * Build and bootstrap xocument.
     * @param path Path for data
     * @return Xocument
     * @throws IOException On failure
     */
    private Xocument xocument(final Path path) throws IOException {
        final Xocument xocument = new Xocument(path);
        if (!this.xsd.isEmpty()) {
            xocument.bootstrap(this.xsd);
        }
        return xocument;
    }
}
