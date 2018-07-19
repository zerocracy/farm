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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.Policy;
import com.zerocracy.cash.Cash;
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Debts;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;

/**
 * The debt is too big.
 * @since 1.0
 */
public final class VsBigDebt implements Votes {

    /**
     * The PMO.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param pkt The PMO
     */
    public VsBigDebt(final Pmo pkt) {
        this.pmo = pkt;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final double vote;
        final Debts debts = new Debts(this.pmo).bootstrap();
        final Cash max = new Policy().get("3.max-debt", Cash.ZERO);
        if (debts.exists(login)
            && debts.amount(login).compareTo(max) > 0) {
            log.append("The debt is too big");
            vote = 1.0D;
        } else {
            log.append("The debt is absent or small enough");
            vote = 0.0D;
        }
        return vote;
    }
}
