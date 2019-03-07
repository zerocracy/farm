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
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.pm.staff.GlobalInviters;
import com.zerocracy.pmo.Resumes;
import com.zerocracy.tk.RsPage;
import java.io.IOException;
import java.util.Objects;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rq.RqHref;
import org.xembly.Directives;

/**
 * Render resumes take.
 *
 * @since 1.0
 */
public final class TkResumes implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public TkResumes(final Farm farm) {
        this.farm = farm;
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Response act(final RqRegex req) throws IOException {
        final String login = new RqSecureLogin(this.farm, req).value();
        final String expr;
        final String filter = new RqHref.Smart(req).single("filter", "my");
        final boolean inviter = new GlobalInviters(this.farm).contains(login);
        if (Objects.equals("all", filter) && inviter) {
            expr = "examiner = *";
        } else {
            expr = String.format("examiner = '%s'", login);
        }
        return new RsPage(
            this.farm,
            "/xsl/resumes.xsl",
            req,
            () -> () -> new Directives()
                .add("filter")
                .set(filter)
                .up()
                .add("inviter")
                .set(inviter)
                .up()
                .append(new Resumes(this.farm).bootstrap().filter(expr))
        );
    }
}
