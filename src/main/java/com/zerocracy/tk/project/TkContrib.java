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
package com.zerocracy.tk.project;

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pm.cost.Estimates;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.recharge.Stripe;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Level;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.facets.forward.RsForward;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;

/**
 * Contribution page.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkContrib implements TkRegex {

    /**
     * Revenue threshold.
     */
    private static final BigDecimal THRESHOLD = new BigDecimal("2048");

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkContrib(final Farm frm) {
        this.farm = frm;
    }

    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public Response act(final RqRegex req) throws IOException {
        new RqUser(this.farm, req, false).value();
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
        final BigDecimal rev = new Stripe(this.farm).dailyRevenue("USD");
        Logger.info(this, "/contrib/%s : rev=%s", rev.toString());
        final String key;
        final boolean can = rev.compareTo(TkContrib.THRESHOLD) < 0;
        if (can) {
            key = new Props(this.farm).get("//stripe/key", "");
        } else {
            key = "";
        }
        return new RsPage(
            this.farm,
            "/xsl/contrib.xsl",
            req,
            () -> {
                final String pid = project.pid();
                return new XeChain(
                    new XeAppend("project", pid),
                    new XeAppend(
                        "title",
                        new Catalog(this.farm).bootstrap().title(pid)
                    ),
                    new XeAppend(
                        "stripe_key", key
                    ),
                    new XeAppend(
                        "can_pay", Boolean.toString(can)
                    ),
                    new XeAppend(
                        "balance",
                        new Ledger(this.farm, project).bootstrap().cash().add(
                            new Estimates(this.farm, project).bootstrap()
                                .total().mul(-1L)
                        ).toString()
                    )
                );
            }
        );
    }
}
