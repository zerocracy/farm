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
package com.zerocracy.pmo;

import com.zerocracy.jstk.Item;
import com.zerocracy.pm.Xocument;
import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import org.xembly.Directives;

/**
 * Catalog of all projects.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class Catalog implements Closeable {

    /**
     * Item.
     */
    private final Item item;

    /**
     * Ctor.
     * @param itm Item
     */
    public Catalog(final Item itm) {
        this.item = itm;
    }

    @Override
    public void close() throws IOException {
        this.item.close();
    }

    /**
     * Bootstrap it.
     * @throws IOException If fails
     */
    public void bootstrap() throws IOException {
        new Xocument(this.item.path()).bootstrap("catalog", "pmo/catalog");
    }

    /**
     * Create a project with the given ID.
     * @param pid Project ID
     * @param prefix Prefix
     * @throws IOException If fails
     */
    public void add(final String pid, final String prefix) throws IOException {
        new Xocument(this.item.path()).modify(
            new Directives()
                .xpath("/catalog").add("project")
                .add("id").set(pid).up()
                .add("created")
                .set(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
                .up()
                .add("prefix").set(prefix)
        );
    }

    /**
     * Find a project by XPath query.
     * @param query XPath query
     * @return Prefixes found, if found
     * @throws IOException If fails
     */
    public Collection<String> findByXPath(final String query)
        throws IOException {
        return new Xocument(this.item).xpath(
            String.format("//project[%s]/prefix/text()", query)
        );
    }

}
