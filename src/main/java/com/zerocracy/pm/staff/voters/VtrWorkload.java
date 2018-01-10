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

import com.zerocracy.Par;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.staff.Voter;
import com.zerocracy.pmo.Agenda;
import java.io.IOException;
import org.cactoos.iterable.LengthOf;

/**
 * Workload voter.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.17
 */
public final class VtrWorkload implements Voter {

    /**
     * The PMO.
     */
    private final Project pmo;

    /**
     * Max.
     */
    private final int max;

    /**
     * Ctor.
     * @param prj The PMO
     * @param threshold Max
     */
    public VtrWorkload(final Project prj, final int threshold) {
        this.pmo = prj;
        this.max = threshold;
    }

    @Override
    public double vote(final String login, final StringBuilder log)
        throws IOException {
        final int jobs = new LengthOf(
            new Agenda(this.pmo, login).jobs()
        ).intValue();
        log.append(
            new Par(
                "%d out of %d job(s) in agenda"
            ).say(jobs, this.max)
        );
        return (double) (this.max - Math.min(jobs, this.max))
            / (double) this.max;
    }
}
