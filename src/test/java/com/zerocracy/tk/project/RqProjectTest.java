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
import com.zerocracy.farm.SmartFarm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.fork.RqRegex;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeaders;

/**
 * Test case for {@link RqProject}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RqProjectTest {

    @Test
    public void buildsProject() throws Exception {
        try (final Farm farm = new SmartFarm(new FkFarm())) {
            final Catalog catalog = new Catalog(new Pmo(farm)).bootstrap();
            final String pid = "A1B2C3D4F";
            catalog.add(pid, String.format("2017/07/%s/", pid));
            catalog.link(pid, "github", "test/test");
            final Project project = farm.find(
                String.format("@id='%s'", pid)
            ).iterator().next();
            final Roles roles = new Roles(project).bootstrap();
            final String uid = "yegor256";
            roles.assign(uid, "PO");
            new People(new Pmo(farm)).bootstrap().invite(uid, "mentor");
            MatcherAssert.assertThat(
                new RqProject(
                    farm,
                    new RqRegex.Fake(
                        new RqWithHeaders(
                            new RqFake(),
                            String.format("TkAuth: name=%s;login=%1$s", uid)
                        ),
                        "/p/(.*)",
                        String.format("/p/%s", pid)
                    )
                ).pid(),
                Matchers.equalTo(pid)
            );
        }
    }

    @Test
    public void buildsPmo() throws Exception {
        final Farm raw = new FkFarm();
        new Roles(new Pmo(raw)).bootstrap().assign("yegor256", "PO");
        try (final Farm farm = new SmartFarm(raw)) {
            new People(new Pmo(farm)).bootstrap().invite("yegor256", "mentor");
            MatcherAssert.assertThat(
                new RqProject(
                    farm,
                    new RqRegex.Fake(
                        new RqWithHeaders(
                            new RqFake(),
                            "TkAuth: name=yegor256;login=yegor256"
                        ),
                        "/p/(.*)",
                        "/p/PMO"
                    )
                ).pid(),
                Matchers.equalTo("PMO")
            );
        }
    }

}
