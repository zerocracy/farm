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
package com.zerocracy.tk.rfp;

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Rfps;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import com.zerocracy.tk.RsParFlash;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.forward.RsForward;
import org.takes.rs.xe.XeDirectives;

/**
 * List of all RFPs.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkRfps implements Take {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkRfps(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final Request req) throws IOException {
        return new RsPage(
            this.farm,
            "/xsl/rfps.xsl",
            req,
            () -> {
                final String user = new RqUser(this.farm, req).value();
                final Awards awards = new Awards(this.farm, user).bootstrap();
                final int reputation = awards.total();
                if (awards.total() < new Policy().get("40.min", 0)) {
                    throw new RsForward(
                        new RsParFlash(
                            new Par(
                                "Your reputation (%s) is not big enough",
                                "to see this page, see §40"
                            ).say(reputation),
                            Level.WARNING
                        )
                    );
                }
                return new XeDirectives(
                    new Rfps(this.farm).bootstrap().toXembly()
                );
            }
        );
    }

}
