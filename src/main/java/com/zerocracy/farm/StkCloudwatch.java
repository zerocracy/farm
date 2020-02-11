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

package com.zerocracy.farm;

import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.entry.ExtCloudWatch;
import java.io.IOException;

/**
 * Stakeholder decorator which reports execution time with tags to cloudwatch.
 * @since 1.0
 * @checkstyle LineLengthCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class StkCloudwatch implements Stakeholder {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Origin stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Stk name.
     */
    private final String name;

    /**
     * Ctor.
     * @param farm Farm
     * @param origin Stakeholder
     * @param name Stk name
     */
    public StkCloudwatch(final Farm farm, final Stakeholder origin,
        final String name) {
        this.farm = farm;
        this.origin = origin;
        this.name = name;
    }

    @Override
    public void process(final Project project, final XML xml)
        throws IOException {
        final ClaimIn claim = new ClaimIn(xml);
        final long start = System.currentTimeMillis();
        this.origin.process(project, xml);
        final long end = System.currentTimeMillis();
        final double value = end - start;
        new ExtCloudWatch(this.farm).value().putMetricData(
            new PutMetricDataRequest()
                .withNamespace("0crat/farm")
                .withMetricData(
                    new MetricDatum()
                        .withMetricName(String.format("stk:%s", this.name))
                        .withUnit(StandardUnit.Milliseconds)
                        .withDimensions(
                            new Dimension().withName("scope").withValue("performance"),
                            new Dimension().withName("performance").withValue("stk")
                        )
                        .withValue(value),
                    new MetricDatum()
                        .withMetricName(String.format("pkt:%s", project.pid()))
                        .withUnit(StandardUnit.Milliseconds)
                        .withDimensions(
                            new Dimension().withName("scope").withValue("performance"),
                            new Dimension().withName("performance").withValue("project")
                        )
                        .withValue(value),
                    new MetricDatum()
                        .withMetricName(String.format("claim:%s", claim.type()))
                        .withUnit(StandardUnit.Milliseconds)
                        .withDimensions(
                            new Dimension().withName("scope").withValue("performance"),
                            new Dimension().withName("performance").withValue("claim")
                        )
                        .withValue(value)
                )
        );
    }

}
