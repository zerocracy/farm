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
package com.zerocracy.pmo.banks;

import com.zerocracy.Farm;
import com.zerocracy.cash.Cash;
import com.zerocracy.zold.Zold;
import java.io.IOException;
import java.math.RoundingMode;

/**
 * Zold payment.
 * @since 1.0
 */
public final class BnkZold implements Bank {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public BnkZold(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Cash fee(final Cash amount) {
        return Cash.ZERO;
    }

    @Override
    // @checkstyle ParameterNumberCheck (3 lines)
    public String pay(final String target, final Cash amount,
        final String details, final String unique) throws IOException {
        final Zold zold = new Zold(this.farm);
        zold.pull();
        return zold.pay(
            target,
            amount.decimal().divide(zold.rate(), 2, RoundingMode.FLOOR),
            details
        );
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
