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

import com.zerocracy.Farm;
import com.zerocracy.entry.ExtCloudWatch;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.SolidFunc;

/**
 * Kpi metrics for farm.
 *
 * @since 1.0
 */
public final class KpiOf implements KpiMetrics {

    /**
     * Instance.
     */
    private static final IoCheckedFunc<Farm, KpiMetrics> INSTANCE =
        new IoCheckedFunc<>(
            new SolidFunc<>(
                frm -> {
                    final KpiMetrics kpi;
                    if (new Props(frm).has("//testing")) {
                        kpi = new KpiMetrics.Fake();
                    } else {
                        kpi = new KpiCloudWatch(
                            new ExtCloudWatch(frm).value()
                        );
                    }
                    return kpi;
                }
            )
        );

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public KpiOf(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public void send(final String name, final double value)
        throws IOException {
        KpiOf.INSTANCE.apply(this.farm).send(name, value);
    }

    @Override
    public Set<String> metrics() throws IOException {
        return KpiOf.INSTANCE.apply(this.farm).metrics();
    }

    @Override
    public KpiStats statistic(final String name, final Duration period)
        throws IOException {
        return KpiOf.INSTANCE.apply(this.farm).statistic(name, period);
    }
}
