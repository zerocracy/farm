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
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.pmo.Debts;
import com.zerocracy.pmo.People;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Projects;
import com.zerocracy.tk.RqWithUser;
import com.zerocracy.tk.TkApp;
import com.zerocracy.tk.View;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.cactoos.list.ListOf;
import org.cactoos.text.FormattedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkProfile}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"})
public final class TkProfileTest {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void rendersHomePage() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final String uid = "yegor";
        new Awards(farm, uid).bootstrap().add(
            new FkProject(), 1, "gh:test/test#1", "reason"
        );
        new Agenda(farm, uid).bootstrap().add(
            new FkProject(), "gh:test/test#2", "QA"
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(new View(farm, "/u/Yegor256").html()),
            XhtmlMatchers.hasXPaths("//xhtml:body")
        );
    }

    @Test
    public void rendersProfilePageWithRateInFirefox() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final double rate = 99.99;
        final People people = new People(farm).bootstrap();
        people.wallet("yegor256", "paypal", "test@example.com");
        people.rate(
            "yegor256", new Cash.S(String.format("USD %f", rate))
        );
        MatcherAssert.assertThat(
            new View(farm, "/u/yegor256").html(),
            Matchers.containsString(
                String.format(
                    "rate</a> is <span style=\"color:darkgreen\">$%.2f</span>",
                    rate
                )
            )
        );
    }

    @Test
    public void rendersProfilePageInFirefoxWithReputation() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final String user = "yegor256";
        MatcherAssert.assertThat(
            new View(farm, String.format("/u/%s", user)).html(),
            XhtmlMatchers.hasXPath(
                String.format(
                    "//xhtml:a[@href='https://github.com/%s']",
                    user
                )
            )
        );
    }

    @Test
    public void rendersProfilePageWithoutRateInFirefox() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final People people = new People(farm).bootstrap();
        final String uid = "yegor256";
        people.wallet(
            uid, "btc", "3HcEB6bi4TFPdvk31Pwz77DwAzfAZz2fMn"
        );
        MatcherAssert.assertThat(
            new View(farm, String.format("/u/%s", uid)).html(),
            Matchers.containsString("rate</a> is not defined")
        );
    }

    @Test
    public void rendersDebts() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Debts debts = new Debts(farm).bootstrap();
        final String uid = "yegor256";
        final String first = "details 1";
        final String second = "details 2";
        final String price = "$9.99";
        final String amount = "$3.33";
        final String reason = "reason";
        debts.add(uid, new Cash.S(price), first, reason);
        debts.add(uid, new Cash.S(amount), second, reason);
        final String html = new View(farm, String.format("/u/%s", uid)).html();
        MatcherAssert.assertThat(
            html,
            Matchers.allOf(
                XhtmlMatchers.hasXPaths(
                    new FormattedText(
                        "//xhtml:td[.='%s (%s)']", first, reason
                    ).asString()
                ),
                XhtmlMatchers.hasXPaths(
                    new FormattedText(
                        "//xhtml:td[.='%s (%s)']", second, reason
                    ).asString()
                ),
                XhtmlMatchers.hasXPaths(
                    new FormattedText("//xhtml:td[.='%s']", price).asString()
                ),
                XhtmlMatchers.hasXPaths(
                    new FormattedText("//xhtml:td[.='%s']", amount).asString()
                )
            )
        );
    }

    @Test
    public void allowsPoToSeeProfile() throws Exception {
        final Path tmp = this.folder.newFolder().toPath();
        final FkProject project = new FkProject(tmp, "AGENDPRJ1");
        final Farm farm = new PropsFarm(new FkFarm(project));
        final Catalog catalog = new Catalog(new Pmo(farm)).bootstrap();
        catalog.add(project.pid(), String.format("2017/07/%s/", project.pid()));
        catalog.link(project.pid(), "github", "test/test");
        final String uid = "krzyk";
        new People(farm).bootstrap().touch(uid);
        new Projects(farm, uid).bootstrap().add(project.pid());
        new Roles(project).bootstrap().assign(uid, "PO");
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    new TkApp(farm).act(
                        new RqWithUser(
                            farm,
                            new RqFake(
                                new ListOf<>(
                                    String.format("GET /u/%s", uid),
                                    "Host: www.example.com",
                                    "Accept: application/vnd.zerocracy+xml"
                                ),
                                ""
                            ),
                            uid,
                            false
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                String.format(
                    "/page/details/projects/project[.='%s']",
                    project.pid()
                )
            )
        );
    }

    @Test
    public void showsLinkToPolicyForUnknownUser() throws Exception {
        final String flash =
            // @checkstyle LineLength (1 line)
            "@nvseenu is not invited to us yet, see <a href=\"www.zerocracy.com/policy.html#1\">§1</a>";
        MatcherAssert.assertThat(
            new View(new PropsFarm(new FkFarm()), "/").html(
                new FormattedText(
                    "Cookie: RsFlash=%s/WARNING",
                    URLEncoder.encode(flash, StandardCharsets.UTF_8.toString())
                ).asString()
            ),
            new StringContains(flash)
        );
    }

}
