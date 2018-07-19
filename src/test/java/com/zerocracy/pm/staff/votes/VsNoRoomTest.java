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
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Pmo;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsNoRoom}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsNoRoomTest {

    @Test
    public void valueDependsOnAwards() throws Exception {
        final Project project = new FkProject();
        final FkFarm farm = new FkFarm();
        final String user = "g4s8";
        final Awards awards = new Awards(farm, user).bootstrap();
        final int points = 2935;
        awards.add(project, points, "gh:test/test#1", "initial");
        final Agenda agenda = new Agenda(farm, user).bootstrap();
        final int total = 10;
        for (int num = 1; num < total; ++num) {
            agenda.add(project, String.format("gh:test/test#%d", num), "QA");
        }
        MatcherAssert.assertThat(
            new VsNoRoom(new Pmo(farm)).take(user, new StringBuilder(0)),
            Matchers.equalTo(0.0)
        );
    }
}
