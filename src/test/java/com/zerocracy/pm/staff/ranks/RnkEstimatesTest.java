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
package com.zerocracy.pm.staff.ranks;

import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.cost.Estimates;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import java.util.Map;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.list.ListOf;
import org.cactoos.list.Sorted;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RnkEstimates}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle DiamondOperatorCheck (500 lines)
 */
public final class RnkEstimatesTest {
    @Test
    public void orderByEstimatedCash() throws Exception {
        final Map<String, Cash> jobs = new MapOf<String, Cash>(
            new MapEntry<>("gh:test/test#1", new Cash.S("$1")),
            new MapEntry<>("gh:test/test#2", new Cash.S("$100")),
            new MapEntry<>("gh:test/test#3", new Cash.S("$42"))
        );
        final FkProject proj = new FkProject();
        final PropsFarm farm = new PropsFarm();
        final Estimates estimates = new Estimates(farm, proj).bootstrap();
        final Orders orders = new Orders(farm, proj).bootstrap();
        final Wbs wbs = new Wbs(proj).bootstrap();
        new Roles(proj).bootstrap().assign("arc", "ARC");
        new Ledger(farm, proj).bootstrap().add(
            new Ledger.Transaction(
                new Cash.S("$10000"),
                "assets", "cash",
                "income", "zerocracy",
                "Send by unit test to skip errors in Estimates.update()"
            )
        );
        for (final Map.Entry<String, Cash> job : jobs.entrySet()) {
            wbs.add(job.getKey());
            orders.assign(job.getKey(), "test", "0");
            estimates.update(job.getKey(), job.getValue());
        }
        MatcherAssert.assertThat(
            new Sorted<>(
                new RnkEstimates(estimates),
                jobs.keySet()
            ),
            Matchers.contains(
                new ListOf<Matcher<? super String>>(
                    Matchers.endsWith("#2"),
                    Matchers.endsWith("#3"),
                    Matchers.endsWith("#1")
                )
            )
        );
    }

    @Test
    public void evaluatesFromCache() throws Exception {
        final String expensive = "gh:test/cached#2";
        MatcherAssert.assertThat(
            new Sorted<>(
                new RnkEstimates(
                    new UncheckedFunc<>(
                        job -> {
                            final Cash cash;
                            if (job.equals(expensive)) {
                                cash = new Cash.S("$101");
                            } else {
                                cash = new Cash.S("$11");
                            }
                            return cash;
                        })
                ),
                new ListOf<>(
                    "gh:test/cached#1",
                    expensive,
                    "gh:test/cached#3"
                )
            ).get(0),
            Matchers.equalTo(expensive)
        );
    }
}
