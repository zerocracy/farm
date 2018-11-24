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
package com.zerocracy.kpi;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * KPI (key performance indicator) metrics.
 *
 * @since 1.0
 */
public interface KpiMetrics {

    /**
     * Send a metric.
     *
     * @param name Metric name
     * @param value Metric value
     * @throws IOException If fails
     */
    void send(String name, double value) throws IOException;

    /**
     * Get all metric names.
     *
     * @return Metric name set
     * @throws IOException If fails
     */
    Set<String> metrics() throws IOException;

    /**
     * Get statistic for metric.
     *
     * @param name Metric name
     * @param period Period to count
     * @return Statistics
     * @throws IOException If fails
     */
    KpiStats statistic(String name, Duration period)
        throws IOException;

    /**
     * Fake metrics.
     */
    final class Fake implements KpiMetrics {

        @Override
        public void send(final String name, final double value) {
            // nothing
        }

        @Override
        public Set<String> metrics() {
            return Collections.emptySet();
        }

        @Override
        public KpiStats statistic(final String name, final Duration period) {
            return KpiStats.EMPTY;
        }
    }
}
