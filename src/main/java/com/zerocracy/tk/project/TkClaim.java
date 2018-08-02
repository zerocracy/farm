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

import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.claims.Footprint;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.logging.Level;
import org.bson.Document;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.SolidList;
import org.cactoos.scalar.IoCheckedScalar;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWithStatus;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeTransform;

/**
 * Single claim take.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @todo #489:30min Change claim.xsl in such a way that it displays the
 *  children of this claim as well. They are returned in the children
 *  element and represent all the claims that have the cause equal to this
 *  claim's id.
 */
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals",
        "PMD.AvoidInstantiatingObjectsInLoops"
    }
)
public final class TkClaim implements TkRegex {
    /**
     * A farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm A farm
     */
    public TkClaim(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex request) throws IOException {
        final RqProject pkt = new RqProject(this.farm, request);
        final String user = new RqUser(this.farm, request, false).value();
        final long cid = Long.valueOf(request.matcher().group(2));
        try (final Footprint ftp = new Footprint(this.farm, pkt)) {
            final Collection<XeSource> children = new SolidList<>(
                new Mapped<>(
                    document -> new XeChain(
                        new XeAppend(
                            "child",
                            new XeTransform<>(
                                document.entrySet(),
                                ent -> new XeAppend(
                                    ent.getKey(),
                                    ent.getValue()
                                        .toString()
                                )
                            )
                        )
                    ),
                    ftp.collection().find(
                        Filters.eq("cause", cid)
                    )
                )
            );
            children.size();
            return new IoCheckedScalar<>(
                new ItemAt<>(
                    0,
                    src -> new RsWithStatus(HttpURLConnection.HTTP_NOT_FOUND),
                    new Mapped<Document, Response>(
                        doc -> new RsPage(
                            this.farm,
                            "/xsl/claim.xsl",
                            request,
                            () -> {
                                final boolean allowed =
                                    doc.keySet().contains("public")
                                    || "PMO".equals(pkt.pid())
                                    || new Roles(pkt).bootstrap()
                                        .hasRole(user, "PO")
                                    || new Roles(new Pmo(this.farm))
                                        .bootstrap().hasAnyRole(user);
                                if (!allowed) {
                                    throw new RsForward(
                                        new RsParFlash(
                                            new Par("Access denied").say(),
                                            Level.WARNING
                                        )
                                    );
                                }
                                return new XeChain(
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
                                    ),
                                    new XeAppend(
                                        "children",
                                        new XeChain(children)
                                    )
                                );
                            }
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
