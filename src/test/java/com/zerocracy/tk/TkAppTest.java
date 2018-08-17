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
package com.zerocracy.tk;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import java.net.HttpURLConnection;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Ignore;
import org.junit.Test;
import org.takes.Take;
import org.takes.http.FtRemote;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkApp}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkAppTest {

    @Test
    public void rendersHomePage() throws Exception {
        final Take take = new TkApp(new PropsFarm(new FkFarm()));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(
                        new RqWithHeader(
                            new RqFake("GET", "/"),
                            "Accept",
                            "text/html"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html",
                "//xhtml:body"
            )
        );
    }

    @Test
    public void rendersHomePageViaHttp() throws Exception {
        final Take app = new TkApp(new PropsFarm(new FkFarm()));
        new FtRemote(app).exec(
            home -> new JdkRequest(home)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(XmlResponse.class)
                .assertXPath("/xhtml:html/xhtml:body")
        );
    }

    // @todo #512:30min 0crat is displaying an error page when trying to use
    //  PsByFlag=PsGithub parameter. This test is triggering this behavior.
    //  Find what is causing it and fix it so this exception screen is not
    //  displayed anymore and un-ingnore the test below.
    @Test
    @Ignore
    public void redirectOnError() throws Exception {
        final Take take = new TkApp(new PropsFarm(new FkFarm()));
        final String message = "Internal application error";
        MatcherAssert.assertThat(
            "Could not redirect on error",
            new TextOf(
                take.act(new RqFake("GET", "/?PsByFlag=PsGithub")).body()
            ).asString(),
            new IsNot<>(
                new StringContains(message)
            )
        );
        MatcherAssert.assertThat(
            "Could not redirect on error (with code)",
            new TextOf(
                take.act(
                    new RqFake(
                        "GET",
                        "/?PsByFlag=PsGithub&code=1257b773ba07221e7eb5"
                    )
                ).body()
            ).asString(),
            new IsNot<>(
                new StringContains(message)
            )
        );
    }
}
