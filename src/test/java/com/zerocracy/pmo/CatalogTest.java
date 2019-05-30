/*
 * Copyright (c) 2016-2019 Zerocracy
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

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Catalog}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods",
        "PMD.AvoidInstantiatingObjectsInLoops", "PMD.ExcessiveImports"
    }
)
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
                    .add("architect").set("0crat").up()
                    .add("members").up()
                    .add("jobs").set(0).up()
                    .add("orders").set(0).up()
                    .add("cash").attr("deficit", false).set(Cash.ZERO).up()
                    .add("languages").up()
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
    public void hasAdviser() throws Exception {
        final String pid = "000000400";
        final Pmo pmo = new Pmo(new FkFarm());
        final Catalog catalog = new Catalog(pmo).bootstrap();
        catalog.add(pid, "2018/10/000000400/");
        MatcherAssert.assertThat(
            "Has adviser but should not",
            catalog.hasAdviser(pid),
            new IsEqual<>(false)
        );
        final String adviser = "user235231";
        catalog.adviser(pid, adviser);
        MatcherAssert.assertThat(
            "Doesn't have adviser but should",
            catalog.hasAdviser(pid),
            new IsEqual<>(true)
        );
    }

    @Test
    public void changeArchitect() throws Exception {
        final String pid = "CHANGEARC";
        final Catalog catalog = CatalogTest.withProject(pid);
        final String arc = "arc";
        catalog.architect(pid, arc);
        MatcherAssert.assertThat(
            catalog.architect(pid),
            new IsEqual<>(arc)
        );
    }

    @Test
    public void changeMembers() throws Exception {
        final String pid = "CHANGEMEM";
        final Catalog catalog = CatalogTest.withProject(pid);
        final String first = "first";
        final String second = "second";
        final String third = "third";
        final ListOf<String> members = new ListOf<>(first, second, third);
        catalog.members(pid, members);
        MatcherAssert.assertThat(
            catalog.members(pid),
            Matchers.contains(first, second, third)
        );
    }

    @Test
    public void changeJobs() throws Exception {
        final String pid = "CHANGEJOB";
        final Catalog catalog = CatalogTest.withProject(pid);
        final int jobs = 42;
        catalog.jobs(pid, jobs);
        MatcherAssert.assertThat(
            catalog.jobs(pid),
            new IsEqual<>(jobs)
        );
    }

    @Test
    public void changeOrders() throws Exception {
        final String pid = "CHANGEORD";
        final Catalog catalog = CatalogTest.withProject(pid);
        final int orders = 12;
        catalog.orders(pid, orders);
        MatcherAssert.assertThat(
            catalog.orders(pid),
            new IsEqual<>(orders)
        );
    }

    @Test
    public void changeCash() throws Exception {
        final String pid = "CHANGECAS";
        final Catalog catalog = CatalogTest.withProject(pid);
        final Cash cash = new Cash.S("$1000.00");
        final boolean deficit = true;
        catalog.cash(pid, cash, deficit);
        MatcherAssert.assertThat(
            "Incorrect cash",
            catalog.cash(pid),
            new IsEqual<>(cash)
        );
        MatcherAssert.assertThat(
            "Incorrect deficit",
            catalog.deficit(pid),
            new IsEqual<>(deficit)
        );
    }

    @Test
    public void changeLanguages() throws Exception {
        final String pid = "CHANGELAN";
        final Catalog catalog = CatalogTest.withProject(pid);
        final String java = "java";
        final String docker = "docker";
        final String shell = "shell";
        final List<String> langs = new ListOf<>(java, docker, shell);
        catalog.languages(pid, new HashSet<>(langs));
        MatcherAssert.assertThat(
            catalog.languages(pid),
            Matchers.containsInAnyOrder(java, docker, shell)
        );
    }

    @Test
    public void collectsActive() throws Exception {
        final Catalog catalog = new Catalog(new FkFarm()).bootstrap();
        final String active = "AAAAAAAAA";
        catalog.add(active, "2018/10/000000401/");
        final String inactive = "IIIIIIIII";
        catalog.add(inactive, "2018/10/000000402/");
        catalog.pause(inactive, true);
        MatcherAssert.assertThat(
            catalog.active(), Matchers.contains(active)
        );
    }

    @Test
    public void changeSandboxFlag() throws Exception {
        final Catalog catalog = new Catalog(new FkFarm()).bootstrap();
        final String pid = "AAAAAAASS";
        catalog.add(pid, "2018/10/000000404/");
        MatcherAssert.assertThat(
            "Project has sandbox flag, but should not",
            catalog.sandbox(pid), Matchers.is(false)
        );
        catalog.sandbox(pid, true);
        MatcherAssert.assertThat(
            "Project doesn't have sandbox flag, but it was added",
            catalog.sandbox(pid), Matchers.is(true)
        );
        catalog.sandbox(pid, false);
        MatcherAssert.assertThat(
            "Project has sandbox flag, but it was removed",
            catalog.sandbox(pid), Matchers.is(false)
        );
    }

    private static Catalog withProject(final String pid) throws IOException {
        final Pmo pmo = new Pmo(new FkFarm());
        final Catalog catalog = new Catalog(pmo).bootstrap();
        catalog.add(pid, "2018/10/000000400/");
        return catalog;
    }

    private static Item item(final Project project) throws IOException {
        return project.acq("catalog.xml");
    }
}
