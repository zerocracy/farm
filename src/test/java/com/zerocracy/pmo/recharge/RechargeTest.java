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
package com.zerocracy.pmo.recharge;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Catalog;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Recharge}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle AvoidDuplicateLiterals (600 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RechargeTest {

    @Test
    public void addsAndRemovesInfo() throws Exception {
        final Farm farm = new FkFarm();
        final Project project = new FkProject();
        new Catalog(farm).bootstrap().add(
            project.pid(),
            String.format("2018/01/%s/", project.pid())
        );
        final Recharge recharge = new Recharge(
            farm, project.pid()
        );
        final Cash amount = new Cash.S("$100");
        recharge.set("stripe", amount, "the code");
        MatcherAssert.assertThat(recharge.exists(), Matchers.is(true));
        MatcherAssert.assertThat(recharge.amount(), Matchers.is(amount));
        recharge.delete();
        MatcherAssert.assertThat(recharge.exists(), Matchers.is(false));
    }

}
