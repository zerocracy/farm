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
package com.zerocracy;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.zerocracy.entry.ExtCloudWatch;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Wrapped project with cloudwatch metrics.
 * @since 1.0
 */
public final class PktCloudwatch implements Project {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Origin project.
     */
    private final Project pkt;

    /**
     * Wrap with cloudwatch metrics.
     * @param farm Farm
     * @param pkt Project to wrap
     */
    public PktCloudwatch(final Farm farm, final Project pkt) {
        this.farm = farm;
        this.pkt = pkt;
    }

    @Override
    public String pid() throws IOException {
        return this.pkt.pid();
    }

    @Override
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public Item acq(final String file) throws IOException {
        return new PktCloudwatch.ItemCloudwatch(
            this.pkt.acq(file),
            new MetricDatum()
                .withMetricName(String.format("item:%s", file))
                .withUnit(StandardUnit.Milliseconds)
                .withDimensions(
                    new Dimension().withName("scope").withValue("performance"),
                    new Dimension().withName("performance").withValue("io")
                ),
            System.currentTimeMillis(),
            this.farm
        );
    }

    /**
     * Wrapped item with cloudwatch metrics.
     * @since 1.0
     */
    private static final class ItemCloudwatch implements Item {

        /**
         * Farm.
         */
        private final Farm farm;

        /**
         * Origin item.
         */
        private final Item origin;

        /**
         * Open timestamp.
         */
        private final long start;

        /**
         * Metric.
         */
        private final MetricDatum metric;

        /**
         * Wrap item.
         * @param origin Item to wrap
         * @param metric Metric data
         * @param start Open timestamp
         * @param farm Farm
         * @checkstyle ParameterNumberCheck (5 lines)
         */
        ItemCloudwatch(final Item origin, final MetricDatum metric,
            final long start, final Farm farm) {
            this.origin = origin;
            this.metric = metric;
            this.start = start;
            this.farm = farm;
        }

        @Override
        public Path path() throws IOException {
            return this.origin.path();
        }

        @Override
        public void close() throws IOException {
            this.origin.close();
            if (new Props(this.farm).has("//testing")) {
                return;
            }
            final long end = System.currentTimeMillis();
            new ExtCloudWatch(this.farm).value().putMetricData(
                new PutMetricDataRequest()
                    .withNamespace("0crat/farm")
                    .withMetricData(
                        this.metric.withValue((double) (end - this.start))
                    )
            );
        }
    }
}
