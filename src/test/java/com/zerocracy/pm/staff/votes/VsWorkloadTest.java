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

import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Projects;
import java.io.IOException;
import org.cactoos.collection.CollectionOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link VsWorkload}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class VsWorkloadTest {

    @Test
    public void votesSmallerTotalAgendaHigher() throws IOException {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm();
        final String krzyk = "krzyk";
        final String llorllale = "llorllale";
        new Projects(farm, krzyk).bootstrap().add(project.pid());
        new Agenda(farm, krzyk).bootstrap()
            .add(project, "none", "DEV");
        final CollectionOf<String> all = new CollectionOf<>(krzyk, llorllale);
        MatcherAssert.assertThat(
            new VsWorkload(farm, all).take(krzyk, new StringBuilder()),
            Matchers.lessThan(
                new VsWorkload(farm, all).take(llorllale, new StringBuilder())
            )
        );
    }

    @Test
    public void votesSmallerProjectAgendaHigher() throws IOException {
        final Project first = new FkProject();
        final Project other = new FkProject("SECOND123");
        final FkFarm farm = new FkFarm();
        final String krzyk = "krzyk";
        final String llorllale = "llorllale";
        new Projects(farm, krzyk).bootstrap().add(first.pid());
        new Projects(farm, llorllale).bootstrap().add(first.pid());
        new Agenda(farm, krzyk).bootstrap()
            .add(first, "none", "DEV");
        final Agenda agenda = new Agenda(farm, llorllale).bootstrap();
        agenda.add(other, "none", "DEV");
        agenda.add(other, "gh:ABC", "DEV");
        final CollectionOf<String> all = new CollectionOf<>(krzyk, llorllale);
        MatcherAssert.assertThat(
            new VsWorkload(farm, first, all).take(krzyk, new StringBuilder()),
            Matchers.lessThan(
                new VsWorkload(farm, first, all)
                    .take(llorllale, new StringBuilder())
            )
        );
        MatcherAssert.assertThat(
            new VsWorkload(farm, other, all).take(krzyk, new StringBuilder()),
            Matchers.greaterThan(
                new VsWorkload(farm, other, all)
                    .take(llorllale, new StringBuilder())
            )
        );
    }
}
