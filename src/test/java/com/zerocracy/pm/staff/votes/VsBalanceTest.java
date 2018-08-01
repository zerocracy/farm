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
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link VsBalance}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsBalanceTest {

    @Test
    public void returnsHighestVoteForEmptyAgenda() throws IOException {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm();
        final String user = "krzyk";
        new Projects(farm, user).bootstrap().add(project.pid());
        MatcherAssert.assertThat(
            new VsBalance(project, farm, new ListOf<>(user))
                .take(user, new StringBuilder()),
            Matchers.is(1.0)
        );
    }

    @Test
    public void votesEmptyProjectHigher() throws IOException {
        final Project first = new FkProject();
        final Project second = new FkProject("SECOND123");
        final FkFarm farm = new FkFarm();
        final String user = "yegor256";
        final Projects projects = new Projects(farm, user).bootstrap();
        projects.add(first.pid());
        new Agenda(farm, user).bootstrap()
            .add(first, "none", "DEV");
        MatcherAssert.assertThat(
            new VsBalance(first, farm, new ListOf<>(user))
                .take(user, new StringBuilder()),
            Matchers.lessThan(
                new VsBalance(second, farm, new ListOf<>(user))
                    .take(user, new StringBuilder())
            )
        );
    }

    @Test
    public void returnsDefaultVoteIfNobodyAssignedToProjects()
        throws IOException {
        final String user = "carlosmiranda";
        MatcherAssert.assertThat(
            new VsBalance(new FkProject(), new FkFarm(), new ListOf<>(user))
                .take(user, new StringBuilder()),
            Matchers.is(1.0)
        );
    }
}
