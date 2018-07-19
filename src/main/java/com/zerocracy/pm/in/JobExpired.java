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
package com.zerocracy.pm.in;

import com.zerocracy.Farm;
import com.zerocracy.Policy;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.time.LocalDateTime;
import org.cactoos.Scalar;

/**
 * Job expired.
 * @since 1.0
 */
public final class JobExpired implements Scalar<Boolean> {
    /**
     * Orders.
     */
    private final Orders orders;
    /**
     * PMO.
     */
    private final Pmo pmo;
    /**
     * Policy.
     */
    private final Policy policy;
    /**
     * Now local time.
     */
    private final LocalDateTime now;
    /**
     * Job id.
     */
    private final String job;

    /**
     * Ctor.
     * @param farm Farm
     * @param orders Orders
     * @param job Job id
     */
    public JobExpired(final Farm farm, final Orders orders, final String job) {
        this(
            new Pmo(farm),
            orders,
            new Policy(farm),
            LocalDateTime.now(),
            job
        );
    }

    /**
     * Ctor.
     * @param pmo PMO
     * @param orders Orders
     * @param policy Policy
     * @param now Local date
     * @param job Job id
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public JobExpired(final Pmo pmo, final Orders orders,
        final Policy policy, final LocalDateTime now, final String job) {
        this.orders = orders;
        this.pmo = pmo;
        this.policy = policy;
        this.now = now;
        this.job = job;
    }

    @Override
    public Boolean value() throws IOException {
        final Awards awards = new Awards(
            this.pmo,
            this.orders.performer(this.job)
        ).bootstrap();
        return this.orders.created(this.job)
            // @checkstyle MagicNumberCheck (1 line)
            .plusDays((long) this.policy.get("8.days", 10))
            .plusDays(
                JobExpired.extra(awards.total())
            ).isBefore(this.now);
    }

    /**
     * Extra days for reputation.
     * @param rep Reputation
     * @return Extra days
     * @checkstyle MagicNumberCheck (12 lines)
     */
    private static long extra(final int rep) {
        final long days;
        if (rep < 512) {
            days = 0L;
        } else if (rep < 2048) {
            days = 2L;
        } else if (rep < 4096) {
            days = 4L;
        } else {
            days = 8L;
        }
        return days;
    }
}
