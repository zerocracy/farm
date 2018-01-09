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

import com.zerocracy.jstk.Project;
import com.zerocracy.pm.staff.Voter;
import com.zerocracy.pmo.Agenda;
import java.io.IOException;
import org.cactoos.iterable.LengthOf;

/**
 * Says "yes" when there is no room for this developer.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 */
public final class VtrNoRoom implements Voter {

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
     * @param pkt Current project
     */
    public VtrNoRoom(final Project pkt) {
        // @checkstyle MagicNumber (1 line)
        this(pkt, 5);
    }

    /**
     * Ctor.
     * @param pkt Current project
     * @param threshold Max
     */
    public VtrNoRoom(final Project pkt, final int threshold) {
        this.pmo = pkt;
        this.max = threshold;
    }

    @Override
    public double vote(final String login, final StringBuilder log)
        throws IOException {
        final int total = new LengthOf(
            new Agenda(this.pmo, login).bootstrap().jobs()
        ).intValue();
        final double rate;
        if (total >= this.max) {
            rate = 1.0d;
            log.append(
                String.format(
                    "%d open jobs already, max is %d", total, this.max
                )
            );
        } else if (total > 1) {
            rate = 0.0d;
            log.append(
                String.format(
                    "Just %d open jobs out of %d", total, this.max
                )
            );
        } else if (total == 1) {
            rate = 0.0d;
            log.append(
                String.format(
                    "There is just one open job out of %d", this.max
                )
            );
        } else {
            rate = 0.0d;
            log.append(
                String.format(
                    "There are no jobs yet, max is %d", this.max
                )
            );
        }
        return rate;
    }

}
