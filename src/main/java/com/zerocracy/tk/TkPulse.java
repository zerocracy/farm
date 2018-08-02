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

import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.claims.Footprint;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.json.Json;
import org.bson.conversions.Bson;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsJson;

/**
 * Pulse.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkPulse implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    TkPulse(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Bson since = Filters.gt(
            "created",
            Date.from(
                ZonedDateTime.now()
                    .minus(1L, ChronoUnit.DAYS)
                    .toInstant()
            )
        );
        try (final Footprint footprint =
            new Footprint(this.farm, new Pmo(this.farm))) {
            return new RsJson(
                Json.createObjectBuilder()
                    .add(
                        "total",
                        footprint.collection().countDocuments(since)
                    )
                    .add(
                        "errors",
                        footprint.collection().countDocuments(
                            Filters.and(since, Filters.eq("type", "Error"))
                        )
                    )
                    .build()
            );
        }
    }

}
