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
package com.zerocracy.tk.profile;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Awards;
import com.zerocracy.tk.RqWithUser;
import com.zerocracy.tk.TkApp;
import java.io.IOException;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkAwards}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.13
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkAwardsTest {

    @Test
    public void rendersXmlAwardsPage() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithUser(
                            farm,
                            new RqFake("GET", "/u/yegor256/awards")
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths("//xhtml:body")
        );
    }

    @Test
    public void rendersHtmlAwardsPageForFirefox() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final String user = "yegor256";
        final int points = 1234;
        new Awards(farm, user).bootstrap()
            .add(new FkProject(), points, "none", "reason");
        final String html = this.firefoxView(farm, user);
        MatcherAssert.assertThat(
            html,
            XhtmlMatchers.hasXPaths("//xhtml:html")
        );
        MatcherAssert.assertThat(
            html,
            Matchers.containsString(String.format("+%d", points))
        );
    }

    private String firefoxView(final Farm farm, final String uid)
        throws IOException {
        return new RsPrint(
            new TkApp(farm).act(
                new RqWithUser(
                    farm,
                    new RqFake(
                        new ListOf<>(
                            String.format("GET /u/%s/awards", uid),
                            "Host: www.example.com",
                            "Accept: application/xml",
                            // @checkstyle LineLength (1 line)
                            "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:62.0) Gecko/20100101 Firefox/62.0"
                        ),
                        ""
                    )
                )
            )
        ).printBody();
    }

}
