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
package com.zerocracy.tk;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.net.URI;
import javax.xml.transform.stream.StreamSource;
import org.cactoos.cache.SoftFunc;
import org.cactoos.func.SyncFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputStreamOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.text.TextOf;
import org.cactoos.time.DateAsText;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeSource;
import org.xembly.Directive;

/**
 * XeSource through XSL.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class XeXsl implements XeSource {

    /**
     * Loader of XSL documents.
     */
    private static final UncheckedFunc<URI, String> STYLESHEETS =
        new UncheckedFunc<>(
            new SyncFunc<>(
                new SoftFunc<>(
                    uri -> new TextOf(new InputOf(uri)).asString()
                )
            )
        );

    /**
     * Project.
     */
    private final Project project;

    /**
     * Item.
     */
    private final String name;

    /**
     * XSL stylesheet name.
     */
    private final String xsl;

    /**
     * Ctor.
     * @param pkt Project
     * @param itm Item
     * @param sheet XSL stylesheet
     */
    public XeXsl(final Project pkt, final String itm, final String sheet) {
        this.project = pkt;
        this.name = itm;
        this.xsl = sheet;
    }

    @Override
    public Iterable<Directive> toXembly() throws IOException {
        try (final Item item = this.project.acq(this.name)) {
            final String content;
            if (item.path().toFile().length() == 0L) {
                content = "<p>The document is empty yet.</p>";
            } else {
                final XML xml = new XMLDocument(item.path().toFile());
                final URI uri = URI.create(
                    String.format(
                        "http://datum.zerocracy.com/latest/xsl/%s",
                        this.xsl
                    )
                );
                content = new XSLDocument(
                    XeXsl.STYLESHEETS.apply(uri),
                    (href, base) -> new StreamSource(
                        new InputStreamOf(
                            XeXsl.STYLESHEETS.apply(
                                URI.create(base).resolve(href)
                            )
                        )
                    ),
                    new SolidMap<String, Object>(
                        new MapEntry<>("today", new DateAsText().asString())
                    ),
                    uri.toString()
                ).transform(xml).nodes("/*/xhtml:body").get(0).toString();
            }
            return new XeAppend("xml", content).toXembly();
        }
    }
}
