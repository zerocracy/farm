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
package com.zerocracy.pmo.banks;

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.Currency;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimOutSafe;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pmo.banks.coinbase.CbTransaction;
import com.zerocracy.pmo.banks.coinbase.Coinbase;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Coinbase payment method.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class Crypto implements Bank {

    /**
     * When to buy.
     */
    private static final BigDecimal THRESHOLD =
        new BigDecimal("0.1");

    /**
     * How much to buy.
     */
    private static final BigDecimal BUYIN =
        new BigDecimal("0.1");

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Currency.
     */
    private final String currency;

    /**
     * Ctor.
     * @param frm The farm
     * @param crn Currency
     */
    Crypto(final Farm frm, final String crn) {
        this.farm = frm;
        this.currency = crn;
    }

    @Override
    public Cash fee(final Cash amount) {
        return amount
            // @checkstyle MagicNumber (1 line)
            .mul((long) (0.01d * (double) Tv.MILLION))
            .div((long) Tv.MILLION);
    }

    @Override
    // @checkstyle ParameterNumberCheck (3 lines)
    public String pay(final String target, final Cash amount,
        final String details, final String unique) throws IOException {
        final Props props = new Props(this.farm);
        final Coinbase base = new Coinbase(
            props.get("//coinbase/key"), props.get("//coinbase/secret"),
            props.get("//coinbase/account")
        );
        this.fund(base);
        final CbTransaction txn = base.send(
            "USD", target,
            amount.exchange(Currency.USD).decimal(),
            details, unique
        );
        new ClaimOutSafe(
            new ClaimOut().type("Notify PMO").param(
                "message",
                new Par(
                    "Coinbase payment has been sent;",
                    "TX=%s, Target=%s,",
                    "Amount=%s"
                ).say(txn.tid(), target, amount)
            )
        ).postTo(new ClaimsOf(this.farm));
        return txn.tid();
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    /**
     * Fund account, if necessary.
     * @param base Base
     * @throws IOException I
     */
    private void fund(final Coinbase base)
        throws IOException {
        final BigDecimal balance = base.balance();
        if (balance.compareTo(Crypto.THRESHOLD) < 0
            && this.permitted(base)) {
            final Coinbase.Bought bought = base.buy(
                this.currency, Crypto.BUYIN,
                true
            );
            new ClaimOut().type("Notify PMO").param(
                "message",
                new Par(
                    "%s purchased @ Coinbase;",
                    "createdAt=%s; transactionId=%s;",
                    "status=%s; balance=%s"
                ).say(
                    bought.amount(),
                    bought.timestamp(),
                    bought.tid(),
                    bought.status(),
                    bought.total()
                )
            ).postTo(new ClaimsOf(this.farm));
        }
    }

    /**
     * We can buy more right now.
     * @param base Base
     * @return TRUE if we're allowed to buy more
     * @throws IOException If fails
     */
    private boolean permitted(final Coinbase base) throws IOException {
        boolean permitted = true;
        int cnt = 0;
        for (final CbTransaction txn : base.transactions()) {
            final boolean pending = txn.status().equals("pending");
            final Instant created = txn.created();
            final BigDecimal amt = txn.amount();
            // @checkstyle LineLength (1 line)
            if (pending && created.isAfter(Instant.now().minus(Duration.ofHours(1L)))) {
                Logger.info(
                    this,
                    // @checkstyle LineLength (1 line)
                    "Funding request is pending since %s, amount=%s, created=%s",
                    created, amt
                );
                permitted = false;
            }
            ++cnt;
            if (cnt > Tv.THIRTY * Tv.TEN) {
                break;
            }
        }
        return permitted;
    }
}
