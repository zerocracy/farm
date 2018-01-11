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
import com.zerocracy.pmo.Agenda;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.cactoos.collection.Filtered;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Workload voter.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VtrWorkload implements Voter {

    /**
     * Workloads of others.
     */
    private final IoCheckedScalar<Map<String, Integer>> jobs;

    /**
     * Ctor.
     * @param pmo The PMO
     * @param others All other logins to compete with
     */
    public VtrWorkload(final Project pmo, final Collection<String> others) {
        this.jobs = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new MapOf<>(
                    new Mapped<>(
                        login -> new MapEntry<>(
                            login,
                            new LengthOf(
                                new Agenda(pmo, login).jobs()
                            ).intValue()
                        ),
                        others
                    )
                )
            )
        );
    }

    @Override
    public double vote(final String login, final StringBuilder log)
        throws IOException {
        final int mine = this.jobs.value().get(login);
        final int smaller = new Filtered<>(
            speed -> speed < mine,
            this.jobs.value().values()
        ).size();
        log.append(
            Logger.format(
                "Workload of %d jobs is no.%d",
                mine, smaller + 1
            )
        );
        return 1.0d - (double) smaller / (double) this.jobs.value().size();
    }
}
