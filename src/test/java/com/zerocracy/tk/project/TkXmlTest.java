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
package com.zerocracy.tk.project;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.tk.TkApp;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeaders;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkXml}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkXmlTest {

    @Test
    public void rendersSingleArtifact() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Catalog catalog = new Catalog(new Pmo(farm)).bootstrap();
        final String pid = "A1B2C3D4F";
        catalog.add(pid, String.format("2017/07/%s/", pid));
        final Roles roles = new Roles(
            farm.find(String.format("@id='%s'", pid)).iterator().next()
        ).bootstrap();
        final String uid = "yegor256";
        roles.assign(uid, "PO");
        new People(new Pmo(farm)).bootstrap().invite(uid, "mentor");
        final Take take = new TkApp(farm);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(
                        new RqWithHeaders(
                            new RqFake(
                                "GET",
                                String.format(
                                    "/xml/%s?file=roles.xml", pid
                                )
                            ),
                            // @checkstyle LineLength (1 line)
                            "Cookie: PsCookie=0975A5A5-F6DB193E-AF18000A-75726E3A-74657374-3A310005-6C6F6769-6E000879-65676F72-323536AE"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths("/roles")
        );
    }

}
