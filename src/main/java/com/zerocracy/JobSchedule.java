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
     * Ctor.
     * @param farm Farm
     * @param orders Orders
     */
    public JobSchedule(final Farm farm, final Orders orders) {
        this(new Pmo(farm), orders, new Policy(farm));
    }

    /**
     * Ctor.
     * @param pmo PMO
     * @param orders Orders
     * @param policy Policy
     */
    public JobSchedule(final Pmo pmo, final Orders orders,
        final Policy policy) {
        this.orders = orders;
        this.pmo = pmo;
        this.policy = policy;
    }

    /**
     * Resign date for job.
     * @param job Job id
     * @return Local date of resign
     * @throws IOException If fails
     */
    public LocalDateTime resign(final String job) throws IOException {
        return this.orders.created(job)
            // @checkstyle MagicNumberCheck (1 line)
            .plusDays((long) this.policy.get("8.days", 10))
            .plusDays(
                JobSchedule.extra(
                    new Awards(this.pmo, this.orders.performer(job)).total()
                )
            );
    }

    /**
     * Extra days for reputation.
     * @param rep Reputation
     * @return Extra days
     * @checkstyle MagicNumberCheck (12 lines)
     */
    private static long extra(final int rep) {
        final int days;
        if (rep < 251) {
            days = 0;
        } else if (rep < 2048) {
            days = 2;
        } else if (rep < 4096) {
            days = 4;
        } else {
            days = 8;
        }
        return days;
    }
}
