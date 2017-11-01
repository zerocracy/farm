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
package com.zerocracy.tk.project;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.Footprint;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.bson.Document;
import org.cactoos.list.StickyList;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeTransform;

/**
 * Footprint page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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
        return new RsPage(
            this.farm,
            "/xsl/footprint.xsl",
            req,
            () -> {
                final Project project = new RqProject(this.farm, req);
                final Collection<Document> docs;
                try (final Footprint footprint =
                    new Footprint(this.farm, project)) {
                    docs = new StickyList<>(
                        footprint.collection()
                            .find(Filters.eq("project", project.pid()))
                            .sort(Sorts.descending("created"))
                            // @checkstyle MagicNumber (1 line)
                            .limit(50)
                    );
                    docs.size();
                }
                return new XeChain(
                    new XeAppend("project", project.pid()),
                    new XeAppend(
                        "claims",
                        new XeTransform<>(
                            docs,
                            doc -> new XeAppend(
                                "claim",
                                new XeTransform<Map.Entry<String, Object>>(
                                    doc.entrySet(),
                                    ent -> new XeAppend(
                                        ent.getKey(),
                                        ent.getValue().toString()
                                    )
                                )
                            )
                        )
                    )
                );
            }
        );
    }

}
