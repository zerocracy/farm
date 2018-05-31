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

import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.footprint.FtFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.tk.RqWithUser;
import com.zerocracy.tk.TkApp;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeaders;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkClaim}.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkClaimTest {

    @Test
    public void renderClaimWithNoChildrenXml() throws Exception {
        final Farm farm = new FtFarm(new PropsFarm(new FkFarm()));
        final long cid = 42L;
        final ClaimOut claim = new ClaimOut().type("test").cid(cid);
        claim.postTo(farm.find("@id='C00000000'").iterator().next());
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithHeaders(
                            new RqWithUser(
                                farm,
                                new RqFake(
                                    "GET",
                                    String.format(
                                        "/footprint/C00000000/%d", cid
                                    )
                                )
                            ),
                            "Accept: application/xml"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("/page/claim/cid[text() = %d]", cid),
                "/page/children"
            )
        );
    }

    @Test
    public void renderClaimWithOneChild() throws Exception {
        final FtFarm farm = new FtFarm(new PropsFarm(new FkFarm()));
        final long parent = 111L;
        final long child = 222L;
        final Project proj = farm.find("@id='C00000000'").iterator().next();
        new ClaimOut().type("test").cid(parent).postTo(proj);
        final XML xml = new Claims(proj).iterate().iterator().next();
        new ClaimIn(xml).copy().cid(child).postTo(proj);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithHeaders(
                            new RqWithUser(
                                farm,
                                new RqFake(
                                    "GET",
                                    String.format(
                                        "/footprint/%s/%d", proj.pid(), parent
                                    )
                                )
                            ),
                            "Accept: application/xml"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("/page/claim/cid[text() = %d]", parent),
                String.format("/page/children/child/cid[text() = %d]", child)
            )
        );
    }

    @Test
    public void renderClaimWithManyChildren() throws Exception {
        final FtFarm farm = new FtFarm(new PropsFarm(new FkFarm()));
        final long parent = 164L;
        final int children = Tv.FIFTY;
        final Project proj = farm.find("@id='C00000000'").iterator().next();
        new ClaimOut().type("test").cid(parent).postTo(proj);
        final ClaimIn claim = new ClaimIn(
            new Claims(proj).iterate().iterator().next()
        );
        for (int number = 0; number < children; ++number) {
            claim.copy().cid((long) (Tv.THOUSAND + number))
                .param("number", number).postTo(proj);
        }
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithHeaders(
                            new RqWithUser(
                                farm,
                                new RqFake(
                                    "GET",
                                    String.format(
                                        "/footprint/%s/%d",
                                        proj.pid(),
                                        parent
                                    )
                                )
                            ),
                            "Accept: application/xml"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("/page/claim/cid[text() = %d]", parent),
                String.format("/page/children[count(child) = %d]", children)
            )
        );
    }
}
