/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.fake.FkFarm;
import com.zerocracy.pmo.People;
import com.zerocracy.tk.TkApp;
import java.util.Properties;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkAgenda}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.13
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkAgendaTest {

    @Test
    public void rejectsOnUnknownUser() throws Exception {
        final Take take = new TkApp(new Properties(), new FkFarm());
        MatcherAssert.assertThat(
            new RsPrint(
                take.act(new RqFake("GET", "/u/yegor256/agenda"))
            ).printBody(),
            Matchers.containsString("User \"yegor256\" not found")
        );
    }

    @Test
    public void rendersAgendaPage() throws Exception {
        final Farm farm = new FkFarm();
        new People(farm).bootstrap().touch("jeff");
        final Take take = new TkApp(new Properties(), farm);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(new RqFake("HEAD", "/u/jeff/agenda"))
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html",
                "//xhtml:body"
            )
        );
    }

}
