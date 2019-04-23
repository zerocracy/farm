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
package com.zerocracy.pm.cost;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Txn;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.RangeOf;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Estimates}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class EstimatesTest {

    @Test
    public void showsEmptyTotal() throws Exception {
        final Estimates estimates =
            new Estimates(new PropsFarm(), new FkProject()).bootstrap();
        MatcherAssert.assertThat(
            estimates.total(),
            Matchers.equalTo(Cash.ZERO)
        );
    }

    @Test
    public void estimatesJobs() throws Exception {
        final Project project = new FkProject();
        final PropsFarm farm = new PropsFarm();
        new Ledger(farm, project).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$500"),
                "assets", "cash",
                "income", "sponsor",
                "Funded by Stripe customer"
            )
        );
        final Estimates estimates = new Estimates(farm, project).bootstrap();
        final String first = "gh:yegor256/pdd#4";
        new Wbs(project).bootstrap().add(first);
        new Orders(farm, project).bootstrap()
            .assign(first, "yegor256", UUID.randomUUID().toString());
        estimates.update(first, new Cash.S("$45"));
        MatcherAssert.assertThat(
            estimates.get(first),
            Matchers.equalTo(new Cash.S("$45.00"))
        );
        final String second = "gh:yegor256/pdd#1";
        new Wbs(project).bootstrap().add(second);
        new Orders(farm, project).bootstrap()
            .assign(second, "yegor", UUID.randomUUID().toString());
        estimates.update(second, new Cash.S("$100"));
        MatcherAssert.assertThat(
            estimates.total(),
            Matchers.equalTo(new Cash.S("$145.00"))
        );
    }

    @Test
    public void estimatesJobsWithDifferentCurrencies() throws Exception {
        final Project project = new FkProject();
        final Farm farm = new PropsFarm();
        new Ledger(farm, project).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$500"),
                "assets", "cash",
                "income", "sponsor",
                "Funded by some guy"
            )
        );
        final Estimates estimates = new Estimates(farm, project).bootstrap();
        final String first = "gh:yegor256/pdd#4";
        final Wbs wbs = new Wbs(project).bootstrap();
        wbs.add(first);
        new Orders(farm, project).bootstrap()
            .assign(first, "yegor256", UUID.randomUUID().toString());
        estimates.update(first, new Cash.S("$45"));
        MatcherAssert.assertThat(
            estimates.get(first),
            Matchers.equalTo(new Cash.S("$45.00"))
        );
        final String second = "gh:yegor256/pdd#1";
        wbs.add(second);
        new Orders(farm, project).bootstrap()
            .assign(second, "yegor", UUID.randomUUID().toString());
        estimates.update(second, new Cash.S("€100"));
        MatcherAssert.assertThat(
            estimates.total(),
            Matchers.equalTo(new Cash.S("$177.00"))
        );
    }

    @Test
    public void refreshEstimateWhenJobDeleted() throws Exception {
        final Project pkt = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(pkt));
        new Ledger(farm, pkt).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$500"),
                "assets", "cash",
                "income", "sponsor",
                "Funded by Stripe customer"
            )
        );
        final Wbs wbs = new Wbs(pkt).bootstrap();
        final Orders orders = new Orders(farm, pkt).bootstrap();
        final Estimates est = new Estimates(farm, pkt).bootstrap();
        new And(
            job -> {
                wbs.add(job);
                orders.assign(job, "developer", "test");
                est.update(job, new Cash.S("$16"));
            },
            new Mapped<>(
                jid -> String.format("gh:test/test#%d", jid),
                new RangeOf<Integer>(1, Tv.TEN, x -> x + 1)
            )
        ).value();
        try (final Item item = pkt.acq("estimates.xml")) {
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/estimates/order[@id='gh:test/test#1']")
                    .remove()
            );
        }
        MatcherAssert.assertThat(
            est.total(), Matchers.equalTo(new Cash.S("$144"))
        );
    }

    @Test
    @Ignore
    public void calculatesTotal() throws Exception {
        final Farm farm = new PropsFarm();
        final Project pkt = new FkProject();
        new Ledger(farm, pkt).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$1000000"),
                "assets", "cash",
                "income", "sponsor",
                "Funded by Stripe customer"
            )
        );
        try (final Txn txn = new Txn(pkt)) {
            final Wbs wbs = new Wbs(txn).bootstrap();
            final Estimates est = new Estimates(farm, txn).bootstrap();
            final Orders orders = new Orders(farm, txn).bootstrap();
            new And(
                cnt -> {
                    final String job = String.format("gh:test/test#%d", cnt);
                    wbs.add(job);
                    orders.assign(job, "test", "estimating");
                    est.update(job, new Cash.S(String.format("$%s", cnt)));
                },
                new RangeOf<>(1, Tv.THREE * Tv.HUNDRED, num -> num + 1)
            ).value();
            txn.commit();
        }
        try (final Txn txn = new Txn(pkt)) {
            final Estimates est = new Estimates(farm, txn).bootstrap();
            final long start = System.nanoTime();
            final Cash total = est.total();
            final long end = System.nanoTime();
            MatcherAssert.assertThat(
                total, Matchers.equalTo(new Cash.S("$45150"))
            );
            MatcherAssert.assertThat(
                end - start,
                Matchers.lessThan(
                    TimeUnit.MILLISECONDS.toNanos((long) Tv.TWENTY)
                )
            );
        }
    }
}
