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
package com.zerocracy.pmo;

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import org.cactoos.list.IterableAsMap;
import org.cactoos.list.TransformedIterable;
import org.xembly.Directives;

/**
 * External data.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class Ext {

    /**
     * Project.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Ext(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public Ext(final Project pkt) {
        this.pmo = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Ext bootstrap() throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).bootstrap("pmo/ext");
        }
        return this;
    }

    /**
     * Get one property.
     * @param system The server
     * @param prop Connectivity parameter
     * @return The value
     * @throws IOException If fails
     */
    public String get(final String system, final String prop)
        throws IOException {
        try (final Item item = this.item()) {
            return new Xocument(item.path()).xpath(
                String.format(
                    "/ext/system[@id='%s']/prop[@id='%s']/text()",
                    system, prop
                )
            ).get(0);
        }
    }

    /**
     * Get all properties of the system.
     * @param system The server
     * @return The props
     * @throws IOException If fails
     */
    public Map<String, String> get(final String system) throws IOException {
        try (final Item item = this.item()) {
            return new IterableAsMap<>(
                new TransformedIterable<>(
                    new Xocument(item.path()).nodes(
                        String.format(
                            "/ext/system[@id='%s']/prop",
                            system
                        )
                    ),
                    xml -> new AbstractMap.SimpleEntry<>(
                        xml.xpath("@id").get(0),
                        xml.xpath("text()").get(0)
                    )
                )
            );
        }
    }

    /**
     * Set one property.
     * @param system The server
     * @param prop Connectivity parameter
     * @param value Value to set
     * @return This
     * @throws IOException If fails
     */
    public Ext set(final String system, final String prop,
        final String value) throws IOException {
        try (final Item item = this.item()) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath(
                        String.format(
                            "/ext[not(system/@id='%s')]",
                            system
                        )
                    )
                    .add("system")
                    .attr("id", system)
                    .xpath(
                        String.format(
                            "/ext/system[@id='%s']/prop[@id='%s']",
                            system, prop
                        )
                    )
                    .remove()
                    .xpath(
                        String.format(
                            "/ext/system[@id='%s']",
                            system
                        )
                    )
                    .add("prop")
                    .attr("id", prop)
                    .set(value)
            );
        }
        return this;
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.pmo.acq("ext.xml");
    }

}
