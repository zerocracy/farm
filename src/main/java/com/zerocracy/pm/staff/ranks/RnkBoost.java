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
package com.zerocracy.pm.staff.ranks;

import com.zerocracy.pm.cost.Boosts;
import java.util.Comparator;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * Give higher rank for boosted jobs.
 *
 * @since 1.0
 */
public final class RnkBoost implements Comparator<String> {
    /**
     * Cached factor comparator.
     */
    private final UncheckedFunc<String, Integer> cmp;

    /**
     * Ctor.
     * @param boosts Boosts
     */
    public RnkBoost(final Boosts boosts) {
        this.cmp = new UncheckedFunc<>(
            new SolidFunc<>(job -> boosts.factor(job))
        );
    }

    @Override
    public int compare(final String left, final String right) {
        return Integer.compare(this.cmp.apply(right), this.cmp.apply(left));
    }
}
