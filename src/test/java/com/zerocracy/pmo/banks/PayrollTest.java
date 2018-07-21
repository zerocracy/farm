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

import com.zerocracy.Par;
import com.zerocracy.SoftException;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pm.cost.Ledger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link Payroll}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle JavadocVariableCheck (500 lines)
 */
public final class PayrollTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void amountTooSmall() throws Exception {
        final Cash amount = new Cash.S("$0.1");
        final String reason = "Some payment";
        this.expected.expect(SoftException.class);
        this.expected.expectMessage(
            new Par(
                "The amount %s is too small at: %s"
            ).say(amount, reason)
        );
        new Payroll(new FkFarm()).pay(
            new Ledger(
                new FkProject()
            ),
            "paulodamaso",
            amount,
            reason
        );
    }

    @Test (expected = UnsupportedOperationException.class)
    public void walletIsEmpty() {
        throw new UnsupportedOperationException(
            "walletIsEmpty test is not implemented"
        );
    }

    @Test (expected = UnsupportedOperationException.class)
    public void unsupportedPaymentMethod() {
        throw new UnsupportedOperationException(
            "unsupportedPaymentMethod test is not implemented"
        );
    }

    @Test (expected = UnsupportedOperationException.class)
    public void makePayment() {
        throw new UnsupportedOperationException(
            "makePayment test is not implemented"
        );
    }
}
