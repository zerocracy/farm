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
package com.zerocracy.pm.staff.votes;

import com.jcabi.log.Logger;
import com.zerocracy.Project;
import com.zerocracy.pm.staff.Elections;
import com.zerocracy.pm.staff.Votes;
import java.io.IOException;
import org.cactoos.collection.Filtered;
import org.cactoos.iterable.LengthOf;

/**
 * Make it impossible to over-elect one user.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.26
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class VsOverElected implements Votes {

    /**
     * PMO.
     */
    private final Project pkt;

    /**
     * Elections limit.
     */
    private final int limit;

    /**
     * Ctor.
     * @param project The project
     * @param max Max jobs in the agenda
     */
    public VsOverElected(final Project project, final int max) {
        this.pkt = project;
        this.limit = max;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final Elections elections = new Elections(this.pkt).bootstrap();
        final int mine = new LengthOf(
            new Filtered<>(
                job -> elections.winner(job).equals(login),
                elections.jobs()
            )
        ).intValue();
        final double vote;
        if (mine > this.limit) {
            log.append(
                Logger.format(
                    "%d jobs was elected already, too many (max is %d)",
                    mine, this.limit
                )
            );
            vote = 1.0d;
        } else {
            log.append(
                Logger.format(
                    "%d jobs was elected, still enough room (max is %d)",
                    mine, this.limit
                )
            );
            vote = 0.0d;
        }
        return vote;
    }
}
