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

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.pm.cost.Estimates;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.cactoos.text.TextOf;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithHeaders;
import org.takes.rs.RsWithType;

/**
 * Project contrib badge.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkContribBadge implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkContribBadge(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Project project = new RqAnonProject(this.farm, req);
        final Catalog catalog = new Catalog(this.farm).bootstrap();
        if (!catalog.fee(project.pid()).equals(Cash.ZERO)) {
            throw new RsForward(
                new RsParFlash(
                    "The project is not free, see §50",
                    Level.WARNING
                ),
                String.format("/p/%s", project.pid())
            );
        }
        final Cash left = new Ledger(project).bootstrap().cash().add(
            new Estimates(project).bootstrap().total().mul(-1L)
        );
        final String amount;
        if (left.equals(Cash.ZERO)) {
            amount = "no money";
        } else {
            amount = String.format("$%s left", left.decimal().intValue());
        }
        return new RsWithHeaders(
            new RsWithType(
                new RsWithBody(
                    new TextOf(
                        this.getClass().getResource("contrib-badge.svg")
                    ).asString().replace("AMOUNT", amount)
                ),
                "image/svg+xml"
            ),
            "Cache-Control: no-cache",
            String.format("X-Zerocracy-Project-ID: %s", project.pid())
        );
    }

}
