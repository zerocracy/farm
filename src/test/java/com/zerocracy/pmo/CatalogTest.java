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
package com.zerocracy.pmo;

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import org.cactoos.time.DateAsText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Catalog}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class CatalogTest {

    @Test
    public void addsAndFindsProjects() throws Exception {
        final Project project = new FkProject();
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
                    .set(new DateAsText().asString()).up()
                    .add("prefix").set("2017/01/AAAABBBBC/").up()
                    .add("fee").set("0").up()
                    .add("alive").set("true").up()
                    .add("publish").set("false").up()
                    .add("adviser").set("0crat").up()
            );
        }
        final Catalog catalog = new Catalog(project);
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
        final Catalog catalog = new Catalog(new FkProject()).bootstrap();
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
        final Catalog catalog = new Catalog(new FkProject()).bootstrap();
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
        final Catalog catalog = new Catalog(new FkProject()).bootstrap();
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
        final Catalog catalog = new Catalog(new FkProject()).bootstrap();
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

    private static Item item(final Project project) throws IOException {
        return project.acq("catalog.xml");
    }

}
