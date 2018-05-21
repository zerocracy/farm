/**
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
import com.zerocracy.pm.cost.Estimates;
import java.util.Comparator;
import org.cactoos.func.SyncBiFunc;
import org.cactoos.func.UncheckedBiFunc;

/**
 * Give higher rank for most expensive tasks.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.22
 */
public final class RnkEstimates implements Comparator<String> {
    /**
     * Global estimates cache.
     */
    private static final UncheckedBiFunc<Estimates, String, Cash> CACHED =
        new UncheckedBiFunc<>(new SyncBiFunc<>(Estimates::get));

    /**
     * Estimates.
     */
    private final Estimates est;
    /**
     * Ctor.
     * @param estimates Estimates
     */
    public RnkEstimates(final Estimates estimates) {
        this.est = estimates;
    }

    @Override
    public int compare(final String left, final String right) {
        return this.cash(right).compareTo(this.cash(left));
    }

    /**
     * Estimated cash of a job.
     * @param job Job id
     * @return Cash
     */
    private Cash cash(final String job) {
        return RnkEstimates.CACHED.apply(this.est, job);
    }
}
