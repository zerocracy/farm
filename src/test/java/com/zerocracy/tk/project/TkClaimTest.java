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
package com.zerocracy.tk.project;

import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.Claims;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.footprint.FtFarm;
import com.zerocracy.tk.TestWithUser;
import com.zerocracy.tk.View;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link TkClaim}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings(
    { "PMD.AvoidDuplicateLiterals", "PMD.TestClassWithoutTestCases" }
)
public final class TkClaimTest extends TestWithUser {

    @Test
    public void renderClaimWithNoChildrenXml() throws Exception {
        final Farm farm = new FtFarm(this.farm);
        final long cid = 42L;
        final ClaimOut claim = new ClaimOut().type("test").cid(cid);
        final Project project = farm.find("@id='C00000000'").iterator().next();
        claim.postTo(new ClaimsOf(farm, project));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(
                    farm, String.format("/footprint/C00000000/%d", cid)
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("/page/claim/cid[text() = %d]", cid),
                "/page/children"
            )
        );
    }

    @Test
    public void renderClaimWithOneChild() throws Exception {
        final FtFarm farm = new FtFarm(this.farm);
        final long parent = 111L;
        final long child = 222L;
        final Project proj = farm.find("@id='C00000000'").iterator().next();
        new ClaimOut().type("test").cid(parent)
            .postTo(new ClaimsOf(farm, proj));
        final XML xml = new ClaimsItem(proj).iterate().iterator().next();
        new ClaimIn(xml).copy().cid(child).postTo(new ClaimsOf(farm, proj));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(
                    farm,
                    String.format("/footprint/%s/%d", proj.pid(), parent)
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("/page/claim/cid[text() = %d]", parent),
                String.format("/page/children/child/cid[text() = %d]", child)
            )
        );
    }

    @Test
    public void renderClaimWithManyChildren() throws Exception {
        final FtFarm farm = new FtFarm(this.farm);
        final long parent = 164L;
        final int children = Tv.FIFTY;
        final Project proj = farm.find("@id='C00000000'").iterator().next();
        final Claims claims = new ClaimsOf(farm, proj);
        new ClaimOut().type("test").cid(parent)
            .postTo(claims);
        final ClaimIn claim = new ClaimIn(
            new ClaimsItem(proj).iterate().iterator().next()
        );
        for (int number = 0; number < children; ++number) {
            claim.copy().cid((long) (Tv.THOUSAND + number))
                .param("number", number)
                .postTo(claims);
        }
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(
                    farm,
                    String.format("/footprint/%s/%d", proj.pid(), parent)
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                String.format("/page/claim/cid[text() = %d]", parent),
                String.format("/page/children[count(child) = %d]", children)
            )
        );
    }
}
