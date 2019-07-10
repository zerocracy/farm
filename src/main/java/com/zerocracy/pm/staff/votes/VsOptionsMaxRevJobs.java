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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Options;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import org.cactoos.text.UncheckedText;

/**
 * Voter that prohibits more than maxRevJobsInAgenda for given user.
 *
 * @since 1.0
 */
public final class VsOptionsMaxRevJobs implements Votes {

    /**
     * The PMO.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param pkt Current project
     */
    public VsOptionsMaxRevJobs(final Pmo pkt) {
        this.pmo = pkt;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final int total = VsOptionsMaxRevJobs.revJobs(
            new Agenda(this.pmo, login).bootstrap()
        ).size();
        final double rate;
        final int max = new Options(this.pmo, login).bootstrap()
            .maxRevJobsInAgenda();
        if (total >= max) {
            rate = 1.0d;
            log.append(
                String.format(
                    "%d REV job(s) already, maxRevJobsInAgenda option is %d",
                    total, max
                )
            );
        } else {
            rate = 0.0d;
            log.append(
                String.format(
                    "%d REV job(s) out of %d from maxRevJobsInAgenda option",
                    total, max
                )
            );
        }
        return rate;
    }

    /**
     * All REV jobs in agenda.
     * @param agenda The agenda.
     * @return All REV jobs in agenda.
     * @throws IOException If something fails.
     */
    private static Collection<String> revJobs(
        final Agenda agenda
    ) throws IOException {
        return agenda.jobs().stream().filter(
            job -> "REV".equals(
                new UncheckedText(() -> agenda.role(job)).asString()
            )
        ).collect(Collectors.toList());
    }
}
