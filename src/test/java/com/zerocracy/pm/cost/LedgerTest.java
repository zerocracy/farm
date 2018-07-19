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

import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Ledger}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class LedgerTest {

    @Test
    public void addsTransactions() throws Exception {
        final Ledger ledger = new Ledger(new FkProject()).bootstrap();
        MatcherAssert.assertThat(ledger.cash(), Matchers.equalTo(Cash.ZERO));
        MatcherAssert.assertThat(
            ledger.add(
                new Ledger.Transaction(
                    new Cash.S("$43"),
                    "assets", "cash",
                    "income", "sponsor",
                    "There is some funding just arrived"
                ),
                new Ledger.Transaction(
                    new Cash.S("$7"),
                    "expenses", "yegor256",
                    "liabilities", "paypal",
                    "Just paid something to paypal"
                )
            ),
            Matchers.equalTo(1L)
        );
        ledger.add(
            new Ledger.Transaction(
                new Cash.S("$100"),
                "assets", "cash",
                "income", "sponsor",
                "The next round of project funding"
            )
        );
        MatcherAssert.assertThat(
            ledger.cash(),
            Matchers.equalTo(new Cash.S("$136"))
        );
    }

    @Test
    public void addsOneTransactions() throws Exception {
        final Ledger ledger = new Ledger(new FkProject()).bootstrap();
        ledger.add(
            new Ledger.Transaction(
                new Cash.S("$77"),
                "assets", "cash",
                "income", "sponsor",
                "There is some funding just arrived to us"
            )
        );
        MatcherAssert.assertThat(
            ledger.cash(),
            Matchers.equalTo(new Cash.S("$77"))
        );
    }

    @Test
    public void modifiesDeficit() throws Exception {
        final Ledger ledger = new Ledger(new FkProject()).bootstrap();
        ledger.deficit(true);
        MatcherAssert.assertThat(
            ledger.deficit(),
            Matchers.equalTo(true)
        );
        ledger.deficit(false);
        MatcherAssert.assertThat(
            ledger.deficit(),
            Matchers.equalTo(false)
        );
    }

}
