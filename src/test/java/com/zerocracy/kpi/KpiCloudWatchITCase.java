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

import com.zerocracy.entry.ExtCloudWatch;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.cactoos.io.BytesOf;
import org.cactoos.text.HexOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.hamcrest.number.IsCloseTo;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link KpiCloudWatch}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class KpiCloudWatchITCase {

    @BeforeClass
    public static void checkProps() throws Exception {
        Assume.assumeTrue(
            "cloudwatch credentials are not provided",
            new Props(new PropsFarm()).has("//cloudwatch")
        );
    }

    @Test
    public void sendMetric() throws Exception {
        final String name = KpiCloudWatchITCase.metric("test1");
        final KpiMetrics kpi = new KpiCloudWatch(
            new ExtCloudWatch(new PropsFarm()).value()
        );
        kpi.send(name, 1.0);
        TimeUnit.MINUTES.sleep(1L);
        MatcherAssert.assertThat(
            kpi.metrics(),
            Matchers.hasItem(name)
        );
    }

    @Test
    public void getStatisticForMetric() throws Exception {
        final String name = KpiCloudWatchITCase.metric("test2");
        final KpiMetrics kpi = new KpiCloudWatch(
            new ExtCloudWatch(new PropsFarm()).value()
        );
        kpi.send(name, 1.0);
        kpi.send(name, 10.0);
        kpi.send(name, 9.0);
        TimeUnit.MINUTES.sleep(1L);
        final KpiStats stats =
            kpi.statistic(name, Duration.ofSeconds(60L));
        MatcherAssert.assertThat(
            "Incorrect count",
            stats.count(),
            new IsEqual<>(3.0)
        );
        MatcherAssert.assertThat(
            "Incorrect MIN",
            stats.min(),
            new IsEqual<>(1.0)
        );
        MatcherAssert.assertThat(
            "Incorrect MAX",
            stats.max(),
            new IsEqual<>(10.0)
        );
        MatcherAssert.assertThat(
            "Incorrect AVG",
            stats.avg(),
            new IsCloseTo(6.66, 0.01)
        );
    }

    /**
     * Make unique metric name from source name.
     *
     * @param name Source name
     * @return Unique name string
     */
    private static String metric(final String name) throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(Instant.now().toEpochMilli());
        return String.format(
            "%s%s", name, new HexOf(new BytesOf(buf.array())).asString()
        );
    }
}
