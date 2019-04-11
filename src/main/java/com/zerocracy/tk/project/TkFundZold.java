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

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.tk.RsPage;
import com.zerocracy.tools.RandomString;
import com.zerocracy.zold.ZldCallbacks;
import com.zerocracy.zold.ZldInvoice;
import com.zerocracy.zold.Zold;
import java.io.IOException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.xembly.Directives;

/**
 * Fund project with zold.
 *
 * @since 1.0
 */
public final class TkFundZold implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public TkFundZold(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final Project pkt = new RqProject(this.farm, req, "PO");
        final Zold zold = new Zold(this.farm);
        zold.pull();
        final ZldInvoice invoice = zold.invoice();
        final String secret = new RandomString(Tv.EIGHT).asString();
        final String code = new RandomString(Tv.EIGHT).asString();
        final String cid = zold.subscribe(
            zold.wallet(),
            invoice.prefix(),
            code,
            "https://www.0crat.com/zcallback",
            secret
        );
        new ZldCallbacks(new ExtDataSource(this.farm).value(), this.farm).add(
            pkt.pid(), cid, code, secret, invoice.prefix()
        );
        return new RsPage(
            this.farm,
            "/xsl/fund-zold.xsl",
            req,
            () -> () -> new Directives()
                .add("project")
                .set(pkt.pid())
                .up()
                .add("invoice")
                .set(invoice.invoice())
                .up()
                .add("code")
                .set(code)
                .up()
                .add("callback")
                .set(cid)
                .up()
                .add("rate")
                .set(zold.rate())
                .up()
        );
    }
}
