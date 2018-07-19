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

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.takes.Take;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;

/**
 * Test case for {@link TkApp}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle JavadocVariableCheck (500 lines)
 * @checkstyle VisibilityModifierCheck (500 lines)
 */
@RunWith(Parameterized.class)
public final class TkAppUrlTest {

    @Parameterized.Parameter
    public String url;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(
            new Object[][] {
                {"/robots.txt"},
                {"/css/main.css"},
                {"/xsl/index.xsl"},
                {"/xsl/layout.xsl"},
                {"/badge/123456789.svg"},
                {"/svg/logo.svg"},
                {"/png/logo.png"},
                {"/"},
                {"/org/takes/rs/xe/memory.xsl"},
                {"/add_to_slack"},
            }
        );
    }

    @Test
    public void rendersAllPossibleUrls() throws Exception {
        final Take take = new TkApp(new PropsFarm(new FkFarm()));
        MatcherAssert.assertThat(
            this.url,
            take.act(new RqFake("GET", this.url)),
            Matchers.not(
                new HmRsStatus(
                    HttpURLConnection.HTTP_NOT_FOUND
                )
            )
        );
    }

}
