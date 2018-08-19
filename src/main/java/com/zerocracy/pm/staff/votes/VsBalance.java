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

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Projects;
import java.io.IOException;
import java.util.Collection;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.MaxOf;
import org.cactoos.scalar.SolidScalar;

/**
 * Most balanced agenda wins.
 *
 * Returns high votes for person that has lower number of tasks in current
 * project and higher in other projects.
 * Returns low votes for persons that have more tasks in current project and
 * lower in others where he/she is a member.
 *
 * @since 1.0
 */
public final class VsBalance implements Votes {
    /**
     * Current project.
     */
    private final Project project;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Max number of projects assigned to a single user.
     */
    private final IoCheckedScalar<Integer> max;

    /**
     * Ctor.
     * @param prj The project
     * @param frm The farm
     * @param others All other logins in the competition
     */
    public VsBalance(final Project prj, final Farm frm,
        final Collection<String> others) {
        this.project = prj;
        this.farm = frm;
        this.max = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> new MaxOf(
                    new Mapped<>(
                        login -> new LengthOf(
                            new Projects(this.farm, login).bootstrap().iterate()
                        ).intValue(),
                        others
                    )
                ).intValue()
            )
        );
    }

    @Override
    public double take(final String login, final StringBuilder log)
        throws IOException {
        final double result;
        if (this.max.value() > 0) {
            final Agenda agenda = new Agenda(this.farm, login).bootstrap();
            final int size = agenda.jobs().size();
            final double percent;
            if (size == 0) {
                percent = 0;
            } else {
                percent = (double) agenda.jobs(this.project).size() / size;
            }
            final int all = new LengthOf(
                new Projects(this.farm, login).bootstrap().iterate()
            ).intValue();
            log.append(
                String.format(
                    "Has %d jobs in %d projects, %.0f%% from current project",
                    size, all, percent * Tv.HUNDRED
                )
            );
            result = 1.0 - (percent * all) / this.max.value();
        } else {
            log.append(
                String.format(
                    "Nobody assigned to any project, using default score"
                )
            );
            result = 1.0;
        }
        return result;
    }
}
