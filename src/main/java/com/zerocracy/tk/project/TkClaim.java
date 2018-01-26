/**
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

import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.pm.Footprint;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.bson.Document;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsWithStatus;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeTransform;

/**
 * Single claim take.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @todo #380:30min Add link to election claim instead of
 *  election full election text in chats. See
 *  https://github.com/zerocracy/farm/issues/380#issue-287764148
 *  for details. It should be in format /footprint/(project)/(claim-id)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkClaim implements TkRegex {
    /**
     * A farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm A farm
     */
    public TkClaim(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final RqRegex request) throws IOException {
        final RqProject pkt = new RqProject(this.farm, request);
        final long cid = Long.valueOf(request.matcher().group(2));
        try (final Footprint ftp = new Footprint(this.farm, pkt)) {
            return new IoCheckedScalar<>(
                new ItemAt<>(
                    0,
                    src -> new RsWithStatus(HttpURLConnection.HTTP_NOT_FOUND),
                    new Mapped<Document, Response>(
                        doc -> new RsPage(
                            this.farm,
                            "/xsl/claim.xsl",
                            request,
                            () -> new XeChain(
                                new XeAppend("project", pkt.pid()),
                                new XeAppend(
                                    "claim",
                                    new XeTransform<>(
                                        doc.entrySet(),
                                        ent -> new XeAppend(
                                            ent.getKey(),
                                            ent.getValue().toString()
                                        )
                                    )
                                )
                            )
                        ),
                        ftp.collection().find(
                            Filters.and(
                                Filters.eq("cid", cid),
                                Filters.eq("project", pkt.pid())
                            )
                        )
                    )
                )
            ).value();
        }
    }
}
