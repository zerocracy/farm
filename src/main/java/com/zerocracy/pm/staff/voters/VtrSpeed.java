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
package com.zerocracy.pm.staff.voters;

import com.jcabi.log.Logger;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.staff.Voter;
import com.zerocracy.pmo.Speed;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Speed voter.
 * <p>
 * Votes for that person who is the fastest.
 * Returns 1 for fast person and 0 for slow, 0.5 - for middle.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.19
 */
public final class VtrSpeed implements Voter {

    /**
     * Max value for slow person.
     */
    private static final double MAX = (double) TimeUnit.DAYS.toMinutes(10L);

    /**
     * A project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt A project.
     */
    public VtrSpeed(final Project pkt) {
        this.project = pkt;
    }

    @Override
    public double vote(final String login, final StringBuilder log)
        throws IOException {
        final double avg = new Speed(this.project, login).bootstrap().avg();
        log.append(
            Logger.format(
                "Average speed is %[ms]s per job",
                (long) avg * TimeUnit.MINUTES.toMillis(1L)
            )
        );
        return (VtrSpeed.MAX - Math.max(0.0, Math.min(VtrSpeed.MAX, avg)))
            / VtrSpeed.MAX;
    }
}
