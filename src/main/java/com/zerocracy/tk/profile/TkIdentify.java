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
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.RqUser;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;

/**
 * Identify.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkIdentify implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    public TkIdentify(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Pmo pmo = new Pmo(this.farm);
        return new RsPage(
            this.farm,
            "/xsl/identify.xsl",
            req,
            () -> {
                final String user = new RqUser(this.farm, req).value();
                final People people = new People(pmo).bootstrap();
                return new XeChain(
                    new XeAppend(
                        "yoti_app_id",
                        new Props(this.farm).get("//yoti/app_id", "")
                    ),
                    new XeAppend(
                        "identification",
                        people.details(user)
                    )
                );
            }
        );
    }
}
