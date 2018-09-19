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

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CloudWatch implementation of {@link KpiMetrics}.
 *
 * @since 1.0
 */
public final class KpiCloudWatch implements KpiMetrics {

    /**
     * Namespace.
     */
    private static final String NAMESPACE = "0crat/farm";

    /**
     * CloudWatch.
     */
    private final AmazonCloudWatch cloudwatch;

    /**
     * Ctor.
     *
     * @param cloudwatch AWS CloudWatch
     */
    public KpiCloudWatch(final AmazonCloudWatch cloudwatch) {
        this.cloudwatch = cloudwatch;
    }

    @Override
    public void send(final String name, final double value) {
        this.cloudwatch.putMetricData(
            new PutMetricDataRequest()
                .withNamespace(KpiCloudWatch.NAMESPACE)
                .withMetricData(
                    new MetricDatum()
                        .withMetricName(name)
                        .withUnit(StandardUnit.None)
                        .withValue(value)
                        .withStorageResolution(1)
                )
        );
    }

    @Override
    public Set<String> metrics() {
        final Set<String> names = new HashSet<>(0);
        final ListMetricsRequest request = new ListMetricsRequest()
            .withNamespace(KpiCloudWatch.NAMESPACE);
        do {
            final ListMetricsResult result =
                this.cloudwatch.listMetrics(request);
            for (final Metric metric : result.getMetrics()) {
                names.add(metric.getMetricName());
            }
            request.setNextToken(result.getNextToken());
        } while (request.getNextToken() != null);
        return names;
    }

    @Override
    public KpiStats statistic(final String name, final Duration period) {
        final List<Datapoint> datapoints = this.cloudwatch.getMetricStatistics(
            new GetMetricStatisticsRequest()
                .withNamespace(KpiCloudWatch.NAMESPACE)
                .withMetricName(name)
                .withStartTime(Date.from(Instant.now().minus(period)))
                .withEndTime(Date.from(Instant.now()))
                .withPeriod((int) period.getSeconds())
                .withStatistics(
                    Statistic.Maximum, Statistic.Minimum,
                    Statistic.Average, Statistic.SampleCount
                )
        ).getDatapoints();
        final KpiStats stats;
        if (datapoints.isEmpty()) {
            stats = KpiStats.EMPTY;
        } else {
            stats = new KpiCloudWatch.CloudWatchStats(datapoints.get(0));
        }
        return stats;
    }

    /**
     * CloudWatch stats implementation.
     */
    private static final class CloudWatchStats implements KpiStats {

        /**
         * CloudWatch data point.
         */
        private final Datapoint datapoint;

        /**
         * Ctor.
         *
         * @param datapoint Data point
         */
        CloudWatchStats(final Datapoint datapoint) {
            this.datapoint = datapoint;
        }

        @Override
        public double avg() {
            return this.datapoint.getAverage();
        }

        @Override
        public double min() {
            return this.datapoint.getMinimum();
        }

        @Override
        public double max() {
            return this.datapoint.getMaximum();
        }

        @Override
        public long count() {
            return this.datapoint.getSampleCount().longValue();
        }
    }
}
