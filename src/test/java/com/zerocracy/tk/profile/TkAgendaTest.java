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
package com.zerocracy.tk.profile;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.People;
import com.zerocracy.tk.RqWithUser;
import com.zerocracy.tk.TkApp;
import com.zerocracy.tk.View;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkAgenda}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkAgendaTest {

    @Test
    public void rendersXmlAgendaPage() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(new PropsFarm(new FkFarm()), "/u/yegor256/agenda")
                    .xml()
            ),
            XhtmlMatchers.hasXPaths("/page")
        );
    }

    @Test
    public void rendersHtmlAgendaPageForFirefox() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                XhtmlMatchers.xhtml(
                    new View(
                        new PropsFarm(new FkFarm()), "/u/yegor256/agenda"
                    ).html()
                )
            ),
            XhtmlMatchers.hasXPaths("//xhtml:body")
        );
    }

    @Test
    public void redirectsWhenAccessingNonexistentUsersAgenda()
        throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        new People(farm).bootstrap().touch("yegor256");
        MatcherAssert.assertThat(
            new RsPrint(
                new TkApp(farm).act(
                    new RqWithUser(
                        farm,
                        new RqFake("GET", "/u/foo-user/agenda")
                    )
                )
            ),
            new HmRsStatus(HttpURLConnection.HTTP_SEE_OTHER)
        );
    }

    @Test
    public void rendersAgendaPage() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Agenda agenda = new Agenda(farm, "yegor256").bootstrap();
        final Project project = new FkProject();
        final String job = "gh:test/test#2";
        agenda.add(project, job, "DEV");
        final String title = "Some random title is here";
        agenda.title(job, title);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(farm, "/u/yegor256/agenda").html()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("//xhtml:td[.='%s']", title)
            )
        );
    }
}
