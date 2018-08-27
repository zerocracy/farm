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

import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.TransactionsResponse;
import com.coinbase.api.entity.Transfer;
import com.coinbase.api.exception.CoinbaseException;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.Currency;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.joda.money.Money;

/**
 * Coinbase payment method.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class Crypto implements Bank {

    /**
     * When to buy.
     */
    private static final double THRESHOLD = 0.1d;

    /**
     * How much to buy.
     */
    private static final double BUYIN = 0.1d;

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
    public String pay(final String target, final Cash amount,
        final String details) throws IOException {
        final Props props = new Props(this.farm);
        final Coinbase base = new CoinbaseBuilder().withApiKey(
            props.get("//coinbase/key"), props.get("//coinbase/secret")
        ).build();
        this.fund(base, props.get("//coinbase/account"));
        final Transaction trn = new Transaction();
        trn.setTo(target);
        trn.setAmount(
            Money.parse(
                String.format(
                    "USD %.2f",
                    amount.exchange(Currency.USD).decimal().doubleValue()
                )
            )
        );
        trn.setNotes(details);
        trn.setInstantBuy(true);
        try {
            return base.sendMoney(trn).getId();
        } catch (final CoinbaseException ex) {
            throw new IOException(
                String.format(
                    "Failed to send %s to %s via Coinbase: %s",
                    trn.getAmount(), trn.getTo(), trn.getNotes()
                ),
                ex
            );
        }
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    /**
     * Fund account, if necessary.
     * @param base Base
     * @param account The account
     * @throws IOException I
     */
    private void fund(final Coinbase base, final String account)
        throws IOException {
        try {
            final Money balance = base.getBalance(account);
            if (balance.getAmount().doubleValue() < Crypto.THRESHOLD
                && this.permitted(base, account)) {
                final Transfer bought = base.buy(this.money(Crypto.BUYIN));
                new ClaimOut().type("Notify PMO").param(
                    "message",
                    new Par(
                        "%s purchased @ Coinbase;",
                        "createdAt=%s; transactionId=%s;",
                        "status=%s; balance=%s"
                    ).say(
                        bought.getBtc(),
                        bought.getCreatedAt(),
                        bought.getTransactionId(),
                        bought.getStatus(),
                        balance
                    )
                ).postTo(new ClaimsOf(this.farm));
            }
        } catch (final CoinbaseException ex) {
            throw new IOException("Failed to buy at Coinbase", ex);
        }
    }

    /**
     * We can buy more right now.
     * @param base Base
     * @param account The account
     * @return TRUE if we're allowed to buy more
     * @throws IOException If fails
     */
    private boolean permitted(final Coinbase base,
        final String account) throws IOException {
        try {
            boolean permitted = true;
            int page = 0;
            while (true) {
                final TransactionsResponse rsp = base.getTransactions(page);
                for (final Transaction trn : rsp.getTransactions()) {
                    // @checkstyle LineLength (1 line)
                    final boolean pending = trn.getDetailedStatus() == Transaction.DetailedStatus.PENDING
                        && trn.getRecipientAddress().equals(account);
                    final long msec = System.currentTimeMillis()
                        - trn.getCreatedAt().toDate().getTime();
                    final long hours = msec / TimeUnit.HOURS.toMillis(1L);
                    if (pending && hours < TimeUnit.DAYS.toHours(1L)) {
                        Logger.info(
                            this,
                            // @checkstyle LineLength (1 line)
                            "Funding request is pending for just %d hours, amount=%s, created=%s",
                            hours, trn.getAmount(), trn.getCreatedAt()
                        );
                        permitted = false;
                    }
                }
                ++page;
                if (!permitted || page >= rsp.getNumPages()
                    || page >= Tv.TWENTY) {
                    break;
                }
            }
            return permitted;
        } catch (final CoinbaseException ex) {
            throw new IOException("Failed to list transactions", ex);
        }
    }

    /**
     * Create money.
     * @param val Double value
     * @return Money
     */
    private Money money(final double val) {
        return Money.parse(String.format("%s %.2f", this.currency, val));
    }

}
