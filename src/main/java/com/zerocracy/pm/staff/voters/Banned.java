/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.pm.staff.voters;

import com.zerocracy.pm.staff.Voter;
import com.zerocracy.pm.staff.bans.Bans;
import java.io.IOException;
import org.cactoos.list.LengthOfIterable;
import org.cactoos.text.JoinedText;

/**
 * Says "yes" when user was banned for electing job.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.13
 */
public final class Banned implements Voter {

    /**
     * Current job.
     */
    private final String job;

    /**
     * Project bans.
     */
    private final Bans bans;

    /**
     * Ctor.
     * @param job Current job
     * @param bans Project bans
     */
    public Banned(final String job, final Bans bans) {
        this.job = job;
        this.bans = bans;
    }

    @Override
    public double vote(final String login, final StringBuilder log)
        throws IOException {
        final Iterable<String> reasons = this.bans.reasons(this.job, login);
        final double rate;
        if (new LengthOfIterable(reasons).value() > 0) {
            log.append("Banned from this job because: ")
                .append(new JoinedText(", ", reasons));
            rate = 1.0;
        } else {
            log.append("There are no bans");
            rate = 0.0;
        }
        return rate;
    }
}
