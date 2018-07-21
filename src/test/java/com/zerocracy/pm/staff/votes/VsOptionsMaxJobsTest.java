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
import com.zerocracy.pmo.Options;
import com.zerocracy.pmo.Pmo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsOptionsMaxJobs}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsOptionsMaxJobsTest {

    @Test
    public void votesHighIfMaxJobsReached() throws Exception {
        final Project project = new FkProject();
        final String user = "g4s8";
        final int total = 10;
        final Pmo pmo = new Pmo(new FkFarm());
        new Options(pmo, user).bootstrap().maxJobsInAgenda(total);
        final Agenda agenda = new Agenda(pmo, user).bootstrap();
        for (int num = 0; num < total; ++num) {
            agenda.add(project, String.format("gh:test/test#%d", num), "DEV");
        }
        MatcherAssert.assertThat(
            new VsOptionsMaxJobs(pmo).take(user, new StringBuilder(0)),
            // @checkstyle MagicNumberCheck (1 lines)
            Matchers.closeTo(1.0, 0.001)
        );
    }

    @Test
    public void votesLowHasRoom() throws Exception {
        final String user = "krzyk";
        final Pmo pmo = new Pmo(new FkFarm());
        new Options(pmo, user).bootstrap().maxJobsInAgenda(1);
        MatcherAssert.assertThat(
            new VsOptionsMaxJobs(pmo).take(user, new StringBuilder(0)),
            // @checkstyle MagicNumberCheck (1 lines)
            Matchers.closeTo(0.0, 0.001)
        );
    }
}
