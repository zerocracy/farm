/**
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
package com.zerocracy;

import com.zerocracy.pm.in.Orders;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Job schedule.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.22
 */
public final class JobSchedule {
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
     * Ctor.
     * @param farm Farm
     * @param orders Orders
     */
    public JobSchedule(final Farm farm, final Orders orders) {
        this(new Pmo(farm), orders, new Policy(farm), LocalDateTime.now());
    }

    /**
     * Ctor.
     * @param pmo PMO
     * @param orders Orders
     * @param policy Policy
     * @param now Local date
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public JobSchedule(final Pmo pmo, final Orders orders,
        final Policy policy, final LocalDateTime now) {
        this.orders = orders;
        this.pmo = pmo;
        this.policy = policy;
        this.now = now;
    }

    /**
     * Does this job expired.
     * @param job Job id
     * @return True if expired
     * @throws IOException If fails
     */
    public boolean expired(final String job) throws IOException {
        final Awards awards = new Awards(this.pmo, this.orders.performer(job))
            .bootstrap();
        return this.orders.created(job)
            // @checkstyle MagicNumberCheck (1 line)
            .plusDays((long) this.policy.get("8.days", 10))
            .plusDays(
                JobSchedule.extra(awards.total())
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
