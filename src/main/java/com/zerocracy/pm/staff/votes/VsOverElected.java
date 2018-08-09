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

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.pm.staff.Elections;
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Awards;
import java.io.IOException;
import org.cactoos.collection.Filtered;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;

/**
 * Make it impossible to over-elect one user.
 *
 * @since 1.0
 * @todo #1433:30min VsOverElected may cause dead-locks in some cases.
 *  Looks like lock for election.xml file can be acquired but never released
 *  for unknown reason (not sure about it). After fix also uncomment line
 *  in elect_performer script with this Votes.
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class VsOverElected implements Votes {

    /**
     * PMO.
     */
    private final Project pkt;

    /**
     * Farm.
     */
    private final Farm frm;

    /**
     * Ctor.
     * @param project The project
     * @param farm Farm
     */
    public VsOverElected(final Project project, final Farm farm) {
        this.pkt = project;
        this.frm = farm;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final Elections elections = new Elections(this.pkt).bootstrap();
        final int mine = new LengthOf(
            new Filtered<>(
                res -> res.elected() && res.winner().equals(login),
                new Mapped<>(elections::result, elections.jobs())
            )
        ).intValue();
        final double vote;
        final int limit = this.threshold(login);
        if (mine > limit) {
            log.append(
                Logger.format(
                    "%d jobs was elected already, too many (max is %d)",
                    mine, limit
                )
            );
            vote = 1.0d;
        } else {
            log.append(
                Logger.format(
                    "%d jobs was elected, still enough room (max is %d)",
                    mine, limit
                )
            );
            vote = 0.0d;
        }
        return vote;
    }

    /**
     * Max jobs for user, depends on awards.
     * @param login A user
     * @return Jobs threshold
     * @throws IOException If fails
     * @todo #926:30min Refactor this method - extract to some class,
     *  because it's used in two different places: here and in VsNoRoom,
     *  this class should encapsulate farm or pmo and accept login as
     *  argument and should get this information from policy,
     *  not hard code it.
     * @checkstyle MagicNumberCheck (20 lines)
     */
    private int threshold(final String login) throws IOException {
        final int points = new Awards(this.frm, login).bootstrap().total();
        final int max;
        if (points < 512) {
            max = 3;
        } else if (points < 2048) {
            max = 8;
        } else if (points < 4096) {
            max = 16;
        } else {
            max = 24;
        }
        return max;
    }
}
