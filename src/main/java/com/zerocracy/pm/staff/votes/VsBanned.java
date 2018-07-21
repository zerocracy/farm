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

import com.zerocracy.Project;
import com.zerocracy.pm.staff.Bans;
import com.zerocracy.pm.staff.Votes;
import java.io.IOException;
import org.cactoos.iterable.LengthOf;
import org.cactoos.text.JoinedText;

/**
 * Says "yes" when user was banned for electing job.
 * @since 1.0
 */
public final class VsBanned implements Votes {

    /**
     * A project.
     */
    private final Project proj;

    /**
     * Current job.
     */
    private final String job;

    /**
     * Ctor.
     * @param job Current job
     * @param proj ApProject
     */
    public VsBanned(final Project proj, final String job) {
        this.proj = proj;
        this.job = job;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final Iterable<String> reasons = new Bans(this.proj)
            .bootstrap()
            .reasons(this.job, login);
        final double rate;
        if (new LengthOf(reasons).intValue() > 0) {
            log.append(
                String.format(
                    "Banned from %s: %s",
                    this.job,
                    new JoinedText(", ", reasons).asString()
                )
            );
            rate = 1.0;
        } else {
            log.append(
                String.format(
                    "There are no bans in %s",
                    this.job
                )
            );
            rate = 0.0;
        }
        return rate;
    }
}
