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

import com.zerocracy.cash.Cash;
import java.io.Closeable;
import java.io.IOException;

/**
 * Bank payment method.
 *
 * @since 1.0
 */
interface Bank extends Closeable {

    /**
     * Calculate payment commission.
     * @param amount The amount
     * @return Fee amount
     * @throws IOException If fails
     */
    Cash fee(Cash amount) throws IOException;

    /**
     * Pay.
     * @param target The target to pay to
     * @param amount The amount to charge
     * @param details Payment details
     * @return Payment ID
     * @throws IOException If fails
     */
    String pay(String target, Cash amount, String details) throws IOException;

}
