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
package com.zerocracy.tk;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkBoard}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.23
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkBoardTest {
    @Test
    public void rendersBoardWithFundedProject() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Repo repo = new MkGithub().randomRepo();
        final Project project = farm.find("").iterator().next();
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(project.pid(), "2017/01/AAAABBBBC/");
        catalog.link(
            project.pid(),
            "github",
            repo.coordinates().repo()
        );
        catalog.publish(project.pid(), true);
        final Ledger ledger = new Ledger(project).bootstrap();
        final Cash.S cash = new Cash.S("$256");
        ledger.add(
            new Ledger.Transaction(
                cash,
                "assets", "cash",
                "income", "sponsor",
                "There is some funding just arrived"
            )
        );
        MatcherAssert.assertThat(
            this.firefoxView(farm),
            XhtmlMatchers.hasXPaths(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "//xhtml:span[@title = 'The project is funded' and . = '%s']",
                    cash
                )
            )
        );
    }

    @Test
    public void rendersBoardWithProjectWithoutFunding() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Repo repo = new MkGithub().randomRepo();
        final Project project = farm.find("").iterator().next();
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(project.pid(), "2017/02/AAAABBBBC/");
        catalog.link(
            project.pid(),
            "github",
            repo.coordinates().repo()
        );
        catalog.publish(project.pid(), true);
        MatcherAssert.assertThat(
            this.firefoxView(farm),
            XhtmlMatchers.hasXPaths(
                // @checkstyle LineLength (1 line)
                "//xhtml:span[@title = 'The project has no funds, you will work for free']"
            )
        );
    }

    private String firefoxView(final Farm farm) throws IOException {
        return new RsPrint(
            new TkApp(farm).act(
                new RqWithUser(
                    farm,
                    new RqFake(
                        new ListOf<>(
                            "GET /board",
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
