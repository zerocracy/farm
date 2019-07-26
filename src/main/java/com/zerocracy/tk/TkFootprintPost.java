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
package com.zerocracy.tk;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.tk.project.RqProject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.UUID;
import org.cactoos.time.DateAsText;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqGreedy;
import org.takes.rq.form.RqFormSmart;
import org.takes.rs.RsWithStatus;
import org.xembly.Directives;

/**
 * Post a claim to the footprint.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkFootprintPost implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm The farm
     */
    TkFootprintPost(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        new ClaimOut(
            new Directives(
                Directives.copyOf(
                    new XMLDocument(
                        new RqFormSmart(new RqGreedy(req))
                            .single("claim")
                    ).node()
                )
            ).xpath("/claim").attr("id", UUID.randomUUID())
                .addIf("created")
                .set(new DateAsText(new Date()).asString())
        ).postTo(
            new ClaimsOf(
                this.farm,
                new RqProject(this.farm, req, "PO")
            )
        );
        return new RsWithStatus(HttpURLConnection.HTTP_NO_CONTENT);
    }
}
