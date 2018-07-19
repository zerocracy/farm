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

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.tk.RqWithUser;
import com.zerocracy.tk.TkApp;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Take;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkArtifact}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkArtifactTest {

    @Test
    public void rejectsAbsentProject() throws Exception {
        final Take take = new TkApp(new PropsFarm(new FkFarm()));
        MatcherAssert.assertThat(
            take.act(
                new RqFake(
                    "HEAD",
                    "/a/ABCDEF098?a=pm/staff/roles"
                )
            ),
            Matchers.not(new HmRsStatus(HttpURLConnection.HTTP_OK))
        );
    }

    @Test
    public void rendersHomePage() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithUser(
                            farm,
                            new RqFake(
                                "GET",
                                "/a/C00000000?a=pm/staff/roles"
                            )
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths("//xhtml:table")
        );
    }

    @Test
    public void rendersWithoutChromeNotice() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final String get = new RsPrint(
            new TkApp(farm).act(
                new RqWithUser(
                    farm,
                    new RqFake(
                        "GET",
                        "/a/C00000000?a=pm/staff/roles"
                    )
                )
            )
        ).printBody();
        MatcherAssert.assertThat(
            get,
            Matchers.not(Matchers.containsString("Chrome"))
        );
    }

}
