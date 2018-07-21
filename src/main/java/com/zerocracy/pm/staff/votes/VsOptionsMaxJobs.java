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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Options;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;

/**
 * Voter that prohibits more than maxJobsInAgenda for given user.
 *
 * @since 1.0
 */
public final class VsOptionsMaxJobs implements Votes {

    /**
     * The PMO.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param pkt Current project
     */
    public VsOptionsMaxJobs(final Pmo pkt) {
        this.pmo = pkt;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final int total = new Agenda(this.pmo, login).bootstrap().jobs().size();
        final double rate;
        final int max = new Options(this.pmo, login).bootstrap()
            .maxJobsInAgenda();
        if (total >= max) {
            rate = 1.0d;
            log.append(
                String.format(
                    "%d job(s) already, maxJobsInAgenda option is %d",
                    total, max
                )
            );
        } else {
            rate = 0.0d;
            log.append(
                String.format(
                    "%d job(s) out of %d from maxJobsInAgenda option",
                    total, max
                )
            );
        }
        return rate;
    }
}
