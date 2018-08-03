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
package com.zerocracy.tk.project;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.Footprint;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.bson.Document;
import org.cactoos.Scalar;
import org.cactoos.list.SolidList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqHref;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeTransform;
import org.takes.rs.xe.XeWhen;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Footprint page.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings
    (
        {
            "PMD.AvoidDuplicateLiterals",
            "PMD.ExcessiveMethodLength",
            "PMD.ExcessiveImports"
        }
    )
public final class TkFootprint implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkFootprint(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String query = new RqHref.Smart(req).single("q", "");
        final int slice = Math.min(
            Integer.parseInt(new RqHref.Smart(req).single("slice", "50")),
            // @checkstyle MagicNumber (1 line)
            200
        );
        final int skip = Integer.parseInt(
            new RqHref.Smart(req).single("skip", "0")
        );
        final BasicDBObject json;
        if (query.isEmpty()) {
            json = new BasicDBObject();
        } else {
            json = BasicDBObject.parse(query);
        }
        final Scalar<XeSource> source = () -> {
            final Project project = new RqProject(this.farm, req, "PO");
            final Collection<Document> docs;
            try (final Footprint footprint =
                new Footprint(this.farm, project)) {
                docs = new SolidList<>(
                    footprint.collection()
                        .find(
                            Filters.and(
                                Filters.eq("project", project.pid()),
                                json
                            )
                        )
                        .sort(Sorts.descending("created"))
                        .limit(slice)
                        .skip(skip)
                );
                docs.size();
            }
            final Catalog catalog = new Catalog(this.farm).bootstrap();
            final String url = String.format(
                "?q=%s&slice=%d&skip=",
                URLEncoder.encode(query, "UTF-8"),
                slice
            );
            return new XeChain(
                new XeAppend("project", project.pid()),
                new XeAppend("title", catalog.title(project.pid())),
                new XeAppend("query", query),
                new XeAppend("skip", Integer.toString(skip)),
                new XeLink(
                    "plain",
                    String.format("%s%d&format=plain", url, skip)
                ),
                new XeLink(
                    "json",
                    String.format("%s%d&format=json", url, skip)
                ),
                new XeWhen(
                    skip >= slice,
                    new XeLink(
                        "back",
                        String.format("%s%d", url, skip - slice)
                    )
                ),
                new XeWhen(
                    !docs.isEmpty(),
                    new XeLink(
                        "next",
                        String.format("%s%d", url, skip + slice)
                    )
                ),
                new XeAppend(
                    "claims",
                    new XeTransform<>(docs, TkFootprint::toSource)
                )
            );
        };
        final String format = new RqHref.Smart(req).single("format", "")
            .toLowerCase(Locale.ENGLISH);
        final Response response;
        if (format.isEmpty()) {
            response = new RsPage(
                this.farm,
                "/xsl/footprint.xsl",
                req,
                source
            );
        } else {
            final XML xml = new XMLDocument(
                new Xembler(
                    new Directives().add("footprint").append(
                        new IoCheckedScalar<>(source).value().toXembly()
                    )
                ).xmlQuietly()
            );
            response = new RsWithType(
                new RsWithBody(
                    XSLDocument.make(
                        TkFootprint.class.getResource(
                            String.format("footprint-%s.xsl", format)
                        )
                    ).applyTo(xml)
                ),
                String.format("text/%s", format)
            );
        }
        return response;
    }

    /**
     * To source.
     * @param doc The claim
     * @return Source
     */
    private static XeSource toSource(final Document doc) {
        return new XeAppend(
            "claim",
            new XeTransform<Map.Entry<String, Object>>(
                new MapOf<String, Object>(
                    new MapOf<>(doc.entrySet()),
                    new MapEntry<>(
                        "ago",
                        Logger.format(
                            "%[ms]s",
                            System.currentTimeMillis()
                                - doc.getDate("created").getTime()
                        )
                    )
                ).entrySet(),
                ent -> new XeAppend(
                    ent.getKey(),
                    ent.getValue().toString()
                )
            )
        );
    }

}
