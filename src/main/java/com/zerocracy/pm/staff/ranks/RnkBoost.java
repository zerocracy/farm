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
package com.zerocracy.pm.staff.ranks;

import com.zerocracy.pm.cost.Boosts;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Give higher rank for boosted jobs.
 *
 * @since 1.0
 */
public final class RnkBoost implements Comparator<String> {
    /**
     * Boosts.
     */
    private final Boosts boosts;
    /**
     * Boost cache.
     */
    private final Map<String, Integer> cache;

    /**
     * Ctor.
     * @param bsts Boosts
     */
    public RnkBoost(final Boosts bsts) {
        this.boosts = bsts;
        this.cache = new HashMap<>(1);
    }

    @Override
    public int compare(final String left, final String right) {
        try {
            return Integer.compare(this.factor(right), this.factor(left));
        } catch (final IOException err) {
            throw new IllegalStateException(err);
        }
    }

    /**
     * Boost factor for a job.
     * @param job A job
     * @return Factor value
     * @throws IOException If fails
     */
    private int factor(final String job) throws IOException {
        final int factor;
        if (this.cache.containsKey(job)) {
            factor = this.cache.get(job);
        } else {
            factor = this.boosts.factor(job);
            this.cache.put(job, factor);
        }
        return factor;
    }
}
