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
package com.zerocracy.tk.rfp;

import com.zerocracy.Farm;
import com.zerocracy.Policy;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.Rfps;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeWhen;

/**
 * One RFP (request for proposal).
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkRfp implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkRfp(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            this.farm,
            "/xsl/rfp.xsl",
            req,
            () -> {
                final String user = new RqUser(this.farm, req, false).value();
                final Rfps rfps = new Rfps(this.farm).bootstrap();
                return new XeChain(
                    new XeAppend(
                        "stripe_key",
                        new Props(this.farm).get(
                            "//stripe/key", ""
                        )
                    ),
                    new XeAppend(
                        "price_cents",
                        Integer.toString(
                            new Policy().get("41.price", Cash.ZERO)
                                // @checkstyle MagicNumber (1 line)
                                .decimal().intValue() * 100
                        )
                    ),
                    new XeWhen(
                        rfps.exists(user),
                        () -> new XeDirectives(rfps.toXembly(user))
                    )
                );
            }
        );
    }

}
