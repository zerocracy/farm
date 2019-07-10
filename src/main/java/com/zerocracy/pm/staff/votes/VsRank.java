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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.pm.staff.Votes;
import java.util.Comparator;
import java.util.Map;
import org.cactoos.collection.Filtered;

/**
 * Rank users by some metric.
 *
 * @param <T> Metric type
 * @since 1.0
 */
abstract class VsRank<T> implements Votes {

    /**
     * Metrics by user.
     */
    private final Map<String, T> metrics;

    /**
     * Metric comparator (1 - better, -1 worse).
     */
    private final Comparator<? super T> cmp;

    /**
     * Ctor.
     * @param metrics Metrics
     * @param cmp Comparator
     */
    protected VsRank(final Map<String, T> metrics, final Comparator<T> cmp) {
        this.metrics = metrics;
        this.cmp = cmp;
    }

    @Override
    public final double take(final String login, final StringBuilder log) {
        final T mine = this.metrics.get(login);
        final int better = new Filtered<>(
            bks -> this.cmp.compare(bks, mine) > 0,
            this.metrics.values()
        ).size();
        log.append(
            String.format(
                "%s %s is no.%d",
                this.getClass().getSimpleName(),
                mine.toString(), better + 1
            )
        );
        return 1.0d - (double) better
            / (double) this.metrics.size();
    }
}
