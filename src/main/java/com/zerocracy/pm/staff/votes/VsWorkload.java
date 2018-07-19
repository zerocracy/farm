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
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Agenda;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.cactoos.Scalar;
import org.cactoos.collection.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Workload voter.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VsWorkload implements Votes {

    /**
     * Workloads of others.
     */
    private final IoCheckedScalar<Map<String, Integer>> jobs;

    /**
     * Ctor.
     * @param farm The farm
     * @param all All other logins to compete with
     */
    public VsWorkload(final Farm farm, final Collection<String> all) {
        this(
            () -> new SolidMap<>(
                new Mapped<>(
                    login -> new MapEntry<>(
                        login,
                        new Agenda(farm, login).bootstrap().jobs().size()
                    ),
                    all
                )
            )
        );
    }
    /**
     * Ctor.
     * @param farm The farm
     * @param pkt Project
     * @param all All other logins to compete with
     */
    public VsWorkload(final Farm farm, final Project pkt,
        final Collection<String> all) {
        this(
            () -> new SolidMap<>(
                new Mapped<>(
                    login -> new MapEntry<>(
                        login,
                        new Agenda(farm, login).bootstrap().jobs(pkt).size()
                    ),
                    all
                )
            )
        );
    }
    /**
     * Primary ctor.
     * @param source Source jobs map
     */
    private VsWorkload(final Scalar<Map<String, Integer>> source) {
        this.jobs = new IoCheckedScalar<>(new SolidScalar<>(source));
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final int mine = this.jobs.value().get(login);
        final int smaller = new Filtered<>(
            size -> size < mine,
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
