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

import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Negligence;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;

/**
 * Downvote user if they've been resigned from a lot of jobs because
 * of being late (spent more than the allowed number of days on it).
 *
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.23
 * @todo #540:30min Test cases are missing for this class. Add some
 *  unit tests and, when Negligence is finished (it is currently in
 *  development), use this voter inside elect_performer.groovy -- make
 *  sure to give it a big weight.
 */
public final class VsNegligence implements Votes {

    /**
     * PMO project.
     */
    private final Pmo pmo;

    /**
     * Ctor.
     * @param pmo PMO project.
     */
    public VsNegligence(final Pmo pmo) {
        this.pmo = pmo;
    }

    @Override
    public double take(
        final String login, final StringBuilder log
    ) throws IOException {
        final double rate;
        if (new Negligence(this.pmo, login).bootstrap().high()) {
            rate = 0.0d;
        } else {
            rate = 1.0d;
        }
        return rate;
    }

}
