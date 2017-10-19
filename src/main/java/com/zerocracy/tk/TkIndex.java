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
package com.zerocracy.tk;

import com.jcabi.log.Logger;
import com.mongodb.client.model.Filters;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pm.Footprint;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;

/**
 * Index page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkIndex implements Take {

    /**
     * When we started.
     */
    private static final long STARTED = System.currentTimeMillis();

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    TkIndex(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            this.farm,
            "/xsl/index.xsl",
            req,
            () -> {
                final long total;
                try (final Footprint footprint =
                    new Footprint(this.farm, new Pmo(this.farm))) {
                    total = footprint.collection().count(
                        Filters.gt(
                            "created",
                            Date.from(
                                ZonedDateTime.now()
                                    .minus(1L, ChronoUnit.WEEKS)
                                    .toInstant()
                            )
                        )
                    );
                }
                return new XeChain(
                    new XeAppend("claims", Long.toString(total)),
                    new XeAppend(
                        "alive",
                        Logger.format(
                            "%[ms]s",
                            System.currentTimeMillis() - TkIndex.STARTED
                        )
                    )
                );
            }
        );
    }

}
