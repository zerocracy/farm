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

import com.zerocracy.cash.Cash;
import com.zerocracy.claims.ClaimIn;
import java.io.IOException;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.SolidFunc;

/**
 * Metric for claim.
 *
 * @since 1.0
 */
public final class MetricFor implements Metric {

    /**
     * Metric for claim.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    private static final IoCheckedFunc<ClaimIn, Metric> FOR_CLAIM =
        new IoCheckedFunc<>(
            new SolidFunc<>(
                clm -> {
                    final Metric metric;
                    switch (clm.type()) {
                        case "Payment was made":
                        case "Payment was added to debts":
                            metric = new Metric.S(
                                "make_payment",
                                new Cash.S(clm.param("amount"))
                                    .decimal().doubleValue()
                            );
                            break;
                        case "Funded by Stripe":
                            metric = new Metric.S(
                                "received_payment",
                                new Cash.S(clm.param("amount"))
                                    .decimal().doubleValue()
                            );
                            break;
                        case "Invite a friend":
                            metric = new Metric.S("user_invited");
                            break;
                        case "Role was assigned":
                            metric = new Metric.S("role_assigned");
                            break;
                        case "Role was resigned":
                            metric = new Metric.S("role_resigned");
                            break;
                        default:
                            metric = Metric.INVALID;
                            break;
                    }
                    return metric;
                }
            )
        );

    /**
     * Claim.
     */
    private final ClaimIn claim;

    /**
     * Ctor.
     *
     * @param claim Claim
     */
    public MetricFor(final ClaimIn claim) {
        this.claim = claim;
    }

    @Override
    public void send(final KpiMetrics kpi) throws IOException {
        MetricFor.FOR_CLAIM.apply(this.claim).send(kpi);
    }
}
