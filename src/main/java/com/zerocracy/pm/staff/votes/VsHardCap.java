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
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;

/**
 * Make it impossible to go over a hard cap for the amount of jobs
 * in the agenda.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VsHardCap implements Votes {

    /**
     * PMO.
     */
    private final Pmo pmo;

    /**
     * Cap.
     */
    private final int cap;

    /**
     * Ctor.
     * @param pkt The PMO
     * @param max Max jobs in the agenda
     */
    public VsHardCap(final Pmo pkt, final int max) {
        this.pmo = pkt;
        this.cap = max;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final int mine = new Agenda(this.pmo, login).bootstrap().jobs().size();
        final double vote;
        if (mine > this.cap) {
            log.append(
                Logger.format(
                    "%d jobs in the agenda already, too many (max is %d)",
                    mine, this.cap
                )
            );
            vote = 1.0d;
        } else {
            log.append(
                Logger.format(
                    "%d jobs in the agenda, still enough room (max is %d)",
                    mine, this.cap
                )
            );
            vote = 0.0d;
        }
        return vote;
    }
}
