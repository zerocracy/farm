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

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.entry.ExtGithub;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pmo.Catalog;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

/**
 * Test case for {@link TkBoard}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkBoardTest {
    @Test
    public void rendersBoardWithFundedProject() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Repo repo = ((MkGithub) new ExtGithub(farm).value())
            .randomRepo();
        final Project project = farm.find("").iterator().next();
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(project.pid(), "2017/01/AAAABBBBC/");
        catalog.link(
            project.pid(),
            "github",
            repo.coordinates().toString()
        );
        catalog.publish(project.pid(), true);
        final Ledger ledger = new Ledger(farm, project).bootstrap();
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
            new View(farm, "/board").html(),
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
        final Repo repo = ((MkGithub) new ExtGithub(farm).value())
            .randomRepo();
        final Project project = farm.find("").iterator().next();
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(project.pid(), "2017/02/AAAABBBBC/");
        catalog.link(
            project.pid(),
            "github",
            repo.coordinates().toString()
        );
        catalog.publish(project.pid(), true);
        MatcherAssert.assertThat(
            new View(farm, "/board").html(),
            XhtmlMatchers.hasXPaths(
                // @checkstyle LineLength (1 line)
                "//xhtml:span[@title = 'The project has no funds, you will work for free']"
            )
        );
    }

    @Test
    public void doesNotShowProjectsNegativeBalance() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Repo repo = ((MkGithub) new ExtGithub(farm).value())
            .randomRepo();
        final Project project = farm.find("").iterator().next();
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(project.pid(), "2017/01/AAAABBBBC/");
        catalog.link(
            project.pid(),
            "github",
            repo.coordinates().toString()
        );
        catalog.publish(project.pid(), true);
        final Ledger ledger = new Ledger(farm, project).bootstrap();
        final Cash.S liabilities = new Cash.S("$256");
        ledger.add(
            new Ledger.Transaction(
                liabilities,
                "expenses", "jobs",
                "liabilities", "debt",
                "Expenses for jobs done"
            )
        );
        ledger.deficit(true);
        MatcherAssert.assertThat(
            "Ledger cash amount should be less than zero.",
            ledger.cash().decimal().signum(),
            new IsEqual<>(-1)
        );
        final String html = new View(farm, "/board").html();
        MatcherAssert.assertThat(
            "Project with negative ledger cash should be shown as 'no funds'.",
            html,
            XhtmlMatchers.hasXPaths(
                // @checkstyle LineLength (1 line)
                "//xhtml:span[@title = 'The project is not properly funded' and . = 'no funds']"
            )
        );
    }

    @Test
    public void displaysLanguages() throws Exception {
        final Farm farm = new PropsFarm(new FkFarm());
        final Repo repo = ((MkGithub) new ExtGithub(farm).value())
            .randomRepo();
        final Project project = farm.find("").iterator().next();
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(project.pid(), "2017/02/AAAABBBBC/");
        catalog.link(
            project.pid(),
            "github",
            repo.coordinates().toString()
        );
        catalog.publish(project.pid(), true);
        MatcherAssert.assertThat(
            new View(farm, "/board").html(),
            XhtmlMatchers.hasXPaths(
                // @checkstyle LineLength (1 line)
                "//xhtml:td[contains(text(), 'Java')]"
            )
        );
    }
}
