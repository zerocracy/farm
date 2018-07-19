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
package com.zerocracy.pm.cost;

import com.zerocracy.Project;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Estimates}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class EstimatesTest {

    @Test
    public void showsEmptyTotal() throws Exception {
        final Estimates estimates = new Estimates(new FkProject()).bootstrap();
        MatcherAssert.assertThat(
            estimates.total(),
            Matchers.equalTo(Cash.ZERO)
        );
    }

    @Test
    public void estimatesJobs() throws Exception {
        final Project project = new FkProject();
        new Ledger(project).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$500"),
                "assets", "cash",
                "income", "sponsor",
                "Funded by Stripe customer"
            )
        );
        final Estimates estimates = new Estimates(project).bootstrap();
        final String first = "gh:yegor256/pdd#4";
        new Wbs(project).bootstrap().add(first);
        new Orders(project).bootstrap().assign(first, "yegor256", 0L);
        estimates.update(first, new Cash.S("$45"));
        MatcherAssert.assertThat(
            estimates.get(first),
            Matchers.equalTo(new Cash.S("$45.00"))
        );
        final String second = "gh:yegor256/pdd#1";
        new Wbs(project).bootstrap().add(second);
        new Orders(project).bootstrap().assign(second, "yegor", 0L);
        estimates.update(second, new Cash.S("$100"));
        MatcherAssert.assertThat(
            estimates.total(),
            Matchers.equalTo(new Cash.S("$145.00"))
        );
    }

    @Test
    public void estimatesJobsWithDifferentCurrencies() throws Exception {
        final Project project = new FkProject();
        new Ledger(project).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$500"),
                "assets", "cash",
                "income", "sponsor",
                "Funded by some guy"
            )
        );
        final Estimates estimates = new Estimates(project).bootstrap();
        final String first = "gh:yegor256/pdd#4";
        final Wbs wbs = new Wbs(project).bootstrap();
        wbs.add(first);
        new Orders(project).bootstrap().assign(first, "yegor256", 0L);
        estimates.update(first, new Cash.S("$45"));
        MatcherAssert.assertThat(
            estimates.get(first),
            Matchers.equalTo(new Cash.S("$45.00"))
        );
        final String second = "gh:yegor256/pdd#1";
        wbs.add(second);
        new Orders(project).bootstrap().assign(second, "yegor", 0L);
        estimates.update(second, new Cash.S("€100"));
        MatcherAssert.assertThat(
            estimates.total(),
            Matchers.equalTo(new Cash.S("$177.00"))
        );
    }

}
