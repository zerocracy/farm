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

import com.zerocracy.Farm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Resumes;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;

/**
 * Render resumes take.
 *
 * @since 1.0
 */
public final class TkResumes implements Take {

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
    public Response act(final Request req) throws IOException {
        final String login = new RqUser(this.farm, req).value();
        final boolean pmo = new Roles(new Pmo(this.farm)).bootstrap()
            .hasAnyRole(login);
        final String expr;
        if (pmo) {
            expr = "examiner = *";
        } else {
            expr = String.format("examiner = '%s'", login);
        }
        return new RsPage(
            this.farm,
            "/xsl/resumes.xsl",
            req,
            () -> () -> new Resumes(this.farm).bootstrap().filter(expr)
        );
    }
}
