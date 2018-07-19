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

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Test case for {@link Rfps}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RfpsTest {

    @Test
    public void addsAndBuysRfp() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Rfps rfps = new Rfps(farm).bootstrap();
        rfps.pay("dmarkov", "paid $25", "dmarkov@zerocracy.com");
        final String login = "yegor256";
        final String email = "yegor256@gmail.com";
        final int rid = rfps.pay(login, "paid $50", email);
        MatcherAssert.assertThat(rid, Matchers.equalTo(2));
        rfps.post(login, "We need this one to work!");
        rfps.post(login, "We changed our mind...");
        MatcherAssert.assertThat(rfps.buy(2, "jeff"), Matchers.equalTo(email));
    }

    @Test
    public void printsListToXembly() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Rfps rfps = new Rfps(farm).bootstrap();
        final String login = "yegor";
        final int rid = rfps.pay(login, "paid $10", "yegor@zerocracy.com");
        MatcherAssert.assertThat(
            rfps.post(login, "This is the work"),
            Matchers.equalTo(1)
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(rfps.toXembly()).xmlQuietly()
            ),
            XhtmlMatchers.hasXPath(
                String.format("/rfps/rfp[id=%d]", rid)
            )
        );
    }

    @Test
    public void printsSingleToXembly() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Rfps rfps = new Rfps(farm).bootstrap();
        final String login = "yegor1";
        final int rid = rfps.pay(login, "paid $7", "yegor@qulice.com");
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(rfps.toXembly(login)).xmlQuietly()
            ),
            XhtmlMatchers.hasXPath(
                String.format("/rfp/id[.=%d]", rid)
            )
        );
    }
}
