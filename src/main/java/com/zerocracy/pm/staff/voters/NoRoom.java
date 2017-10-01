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
public final class NoRoom implements Voter {

    /**
     * The PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param pkt Current project
     */
    public NoRoom(final Project pkt) {
        this.pmo = pkt;
    }

    @Override
    public double vote(final String login, final StringBuilder log)
        throws IOException {
        final long total = new LengthOf(
            new Agenda(this.pmo, login).bootstrap().jobs()
        ).value();
        final double rate;
        // @checkstyle MagicNumber (1 line)
        if (total > 5L) {
            rate = 1.0d;
            log.append(String.format("There are %d open jobs already", total));
        } else if (total > 1L) {
            rate = 0.0d;
            log.append(String.format("There are just %d open jobs", total));
        } else if (total == 1L) {
            rate = 0.0d;
            log.append("There is just one open jobs");
        } else {
            rate = 0.0d;
            log.append("There are no jobs yet");
        }
        return rate;
    }

}
