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
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;

/**
 * Block losers (vote for them).
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VsLosers implements Votes {

    /**
     * PMO.
     */
    private final Pmo pmo;

    /**
     * Reputation threshold.
     */
    private final int threshold;

    /**
     * Ctor.
     * @param pkt The PMO
     * @param min The threshold in reputation
     */
    public VsLosers(final Pmo pkt, final int min) {
        this.pmo = pkt;
        this.threshold = min;
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final int mine = new Awards(this.pmo, login).bootstrap().total();
        final double rank;
        if (mine < this.threshold) {
            rank = 1.0d;
            log.append(
                Logger.format(
                    "Reputation %+d is less than %+d (loser!)",
                    mine, this.threshold
                )
            );
        } else {
            rank = 0.0d;
            log.append(
                Logger.format(
                    "Reputation %+d is above %+d",
                    mine, this.threshold
                )
            );
        }
        return rank;
    }
}
