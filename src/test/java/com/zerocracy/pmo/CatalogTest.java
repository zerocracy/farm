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
package com.zerocracy.pmo;

import com.jcabi.aspects.Tv;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.radars.github.Job;
import java.io.IOException;
import java.time.Instant;
import org.cactoos.text.FormattedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Catalog}.
 * @since 1.0
 * @todo #1333:30min Board page is slow, load all project properties present in
 *  board page in catalog.xml similar it is made in team page. After this,
 *  uncomment test catalogHasBoardPageInfo
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals",
    "PMD.AvoidInstantiatingObjectsInLoops"})
public final class CatalogTest {

    @Test
    public void addsAndFindsProjects() throws Exception {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final String pid = "67WE3343P";
        try (final Item item = CatalogTest.item(project)) {
            new Xocument(item.path()).bootstrap("pmo/catalog");
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/catalog")
                    .add("project")
                    .attr("id", pid)
                    .add("title").set(pid).up()
                    .add("created")
                    .set(Instant.now().toString()).up()
                    .add("prefix").set("2017/01/AAAABBBBC/").up()
                    .add("fee").set("0").up()
                    .add("alive").set("true").up()
                    .add("publish").set("false").up()
                    .add("adviser").set("0crat").up()
            );
        }
        final Catalog catalog = new Catalog(farm);
        catalog.link(pid, "github", "yegor256");
        try (final Item item = CatalogTest.item(project)) {
            MatcherAssert.assertThat(
                new Xocument(item.path()).xpath("//project[links/link]/@id"),
                Matchers.not(Matchers.emptyIterable())
            );
        }
    }

    @Test
    public void changesPublishStatus() throws Exception {
        final String pid = "67WE334FF";
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(pid, "2017/01/67WE334FF/");
        catalog.link(pid, "github", "yegor256/pdd");
        catalog.publish(pid, true);
        MatcherAssert.assertThat(
            catalog.published(pid),
            Matchers.is(true)
        );
    }

    @Test
    public void changesFee() throws Exception {
        final String pid = "67WEDD4FF";
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(pid, "2017/01/67WEDD4FF/");
        MatcherAssert.assertThat(
            catalog.fee(pid),
            Matchers.equalTo(Cash.ZERO)
        );
        catalog.fee(pid, new Cash.S("$5.50"));
        MatcherAssert.assertThat(
            catalog.fee(pid),
            Matchers.equalTo(new Cash.S("USD 5.50"))
        );
    }

    @Test
    public void setsItOnPause() throws Exception {
        final String pid = "67WEPP4FF";
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(pid, "2017/01/67WEPP4FF/");
        MatcherAssert.assertThat(
            catalog.pause(pid),
            Matchers.equalTo(false)
        );
        catalog.pause(pid, true);
        MatcherAssert.assertThat(
            catalog.pause(pid),
            Matchers.equalTo(true)
        );
    }

    @Test
    public void addsAndRemovesLinks() throws Exception {
        final String pid = "67WE334GG";
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Catalog catalog = new Catalog(farm).bootstrap();
        catalog.add(pid, "2017/05/67WE334GG/");
        MatcherAssert.assertThat(catalog.exists(pid), Matchers.is(true));
        final String rel = "jira";
        final String href = "http://example.com:8080";
        catalog.link(pid, rel, href);
        MatcherAssert.assertThat(
            catalog.hasLink(pid, rel, href),
            Matchers.is(true)
        );
        catalog.unlink(pid, rel, href);
        MatcherAssert.assertThat(
            catalog.hasLink(pid, rel, href),
            Matchers.is(false)
        );
    }

    @Test
    public void setProjectTitle() throws Exception {
        final String pid = "000000000";
        final Pmo pmo = new Pmo(new FkFarm());
        final Catalog catalog = new Catalog(pmo).bootstrap();
        catalog.add(pid, "2017/10/000000000/");
        final String title = "test";
        catalog.title(pid, title);
        MatcherAssert.assertThat(
            "tite wasn't changed",
            catalog.title(pid),
            Matchers.equalTo(title)
        );
    }

    @Test
    public void changeAdviser() throws Exception {
        final String pid = "000000100";
        final Pmo pmo = new Pmo(new FkFarm());
        final Catalog catalog = new Catalog(pmo).bootstrap();
        catalog.add(pid, "2017/10/000000100/");
        final String adviser = "user23561";
        catalog.adviser(pid, adviser);
        MatcherAssert.assertThat(
            catalog.adviser(pid),
            Matchers.equalTo(adviser)
        );
    }

    @Test
    @Ignore
    public void catalogHasBoardPageInfo() throws Exception {
        final FkProject project = new FkProject();
        final PropsFarm farm = new PropsFarm();
        new Ledger(farm, project).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$100"),
                "assets", "cash",
                "income", "zerocracy",
                "Current project funds"
            )
        );
        new Catalog(new FkFarm(project)).bootstrap();
        final Github github = new MkGithub();
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("test", false)
        );
        final String arc = "yegor256";
        final String dev = "paulodamaso";
        final Roles roles = new Roles(project).bootstrap();
        roles.assign(arc, "ARC");
        roles.assign(dev, "DEV");
        final Wbs wbs = new Wbs(project).bootstrap();
        final String one = new Job(
            repo.issues().create("Job number one", "")
        ).toString();
        wbs.add(one);
        for (int cont = 0; cont < Tv.THREE; cont = cont + 1) {
            wbs.add(new Job(repo.issues().create("Job", "")).toString());
        }
        new Orders(farm, project).bootstrap().assign(one, dev, "10");
        try (final Item item = CatalogTest.item(project)) {
            MatcherAssert.assertThat(
                "Architect(s) not found",
                new Xocument(item.path()).xpath(
                    new FormattedText(
                        "/catalog/project[@id='%s']/architect",
                        project.pid()
                    ).asString()
                ),
                new IsCollectionContaining<>(new IsEqual<>(arc))
            );
            MatcherAssert.assertThat(
                "Members not found",
                new Xocument(item.path()).xpath(
                    new FormattedText(
                        "/catalog/project[@id='%s']/members",
                        project.pid()
                    ).asString()
                ),
                new IsCollectionContaining<>(new IsEqual<>(dev))
            );
            MatcherAssert.assertThat(
                "Jobs not found",
                new Xocument(item.path()).xpath(
                    new FormattedText(
                        "/catalog/project[@id='%s']/jobs",
                        project.pid()
                    ).asString()
                ),
                new IsNot<>(new IsEmptyIterable<>())
            );
            MatcherAssert.assertThat(
                "Assigned jobs not found",
                new Xocument(item.path()).xpath(
                    new FormattedText(
                        "/catalog/project[@id='%s']/jobs[@assigned]",
                        project.pid()
                    ).asString()
                ),
                new IsEqual<>(1)
            );
            MatcherAssert.assertThat(
                "Total jobs not found",
                new Xocument(item.path()).xpath(
                    new FormattedText(
                        "/catalog/project[@id='%s']/jobs[@total]",
                        project.pid()
                    ).asString()
                ),
                new IsEqual<>(Tv.THREE)
            );
            MatcherAssert.assertThat(
                "Funding info incorrect",
                new Xocument(item.path()).xpath(
                    new FormattedText(
                        "/catalog/project[@id='%s']/funding[@amount]",
                        project.pid()
                    ).asString()
                ),
                new IsEqual<>("$100")
            );
        }
    }

    private static Item item(final Project project) throws IOException {
        return project.acq("catalog.xml");
    }
}
