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

import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Policy;
import com.zerocracy.SoftException;
import com.zerocracy.cash.Cash;
import com.zerocracy.pm.cost.Ledger;
import com.zerocracy.pmo.Debts;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.util.Map;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Payroll.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class Payroll {

    /**
     * Banks we work with.
     */
    private final Map<String, Bank> banks;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    public Payroll(final Farm frm) {
        this.farm = frm;
        this.banks = new MapOf<String, Bank>(
            new MapEntry<>("paypal", new Paypal(frm)),
            new MapEntry<>("btc", new Crypto(frm, "BTC")),
            new MapEntry<>("zld", new Zold(frm))
        );
    }

    /**
     * Pay to someone.
     * @param ledger The ledger to use
     * @param login The login to charge
     * @param amount The amount to charge
     * @param reason The reason
     * @return Payment receipt (short summary of the payment)
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public String pay(final Ledger ledger,
        final String login, final Cash amount,
        final String reason) throws IOException {
        final Cash min = new Policy().get("46.min", new Cash.S("$10"));
        if (amount.compareTo(min) < 0 && !reason.startsWith("Debt repayment")) {
            throw new SoftException(
                new Par(
                    "The amount %s is too small at: %s"
                ).say(amount, reason)
            );
        }
        final People people = new People(this.farm).bootstrap();
        final String wallet = people.wallet(login);
        if (wallet.isEmpty()) {
            throw new SoftException(
                new Par(
                    "@%s doesn't have payment method configured;",
                    "we can't pay %s"
                ).say(login, amount)
            );
        }
        if (new Debts(this.farm).bootstrap().exists(login)
            && !reason.startsWith("Debt repayment")) {
            throw new SoftException(
                new Par(
                    "Debt already exists, adding payment of %s for %s to debts"
                ).say(amount, reason)
            );
        }
        final String method = people.bank(login);
        if (!this.banks.containsKey(method)) {
            throw new SoftException(
                new Par(
                    "@%s has an unsupported payment method \"%s\""
                ).say(login, method)
            );
        }
        final Bank bank = this.banks.get(method);
        final Cash commission = bank.fee(amount);
        final String pid = bank.pay(
            wallet, amount,
            String.format(
                "@%s: %s",
                login,
                new Par.ToText(reason).toString()
            )
        );
        final String text = new Par.ToText(reason).toString();
        ledger.add(
            new Ledger.Transaction(
                amount.add(commission),
                "liabilities", method,
                "assets", "cash",
                new Par("%s (amount:%s, commission:%s, PID:%s)").say(
                    text, amount, commission, pid
                )
            ),
            new Ledger.Transaction(
                commission,
                "expenses", "jobs",
                "liabilities", method,
                new Par("%s (commission)").say(text)
            ),
            new Ledger.Transaction(
                amount,
                "expenses", "jobs",
                "liabilities", String.format("@%s", login),
                text
            )
        );
        return pid;
    }

}
