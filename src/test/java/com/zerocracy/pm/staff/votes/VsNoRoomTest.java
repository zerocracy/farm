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

import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Agenda;
import com.zerocracy.pmo.Awards;
import java.time.LocalDateTime;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsNoRoom}.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsNoRoomTest {

    @Test
    public void valueDependsOnAwards() throws Exception {
        final FkProject project = new FkProject();
        final String user = "g4s8";
        final Awards awards = new Awards(project, user).bootstrap();
        final int points = 2935;
        awards.add(points, "gh:test/test#1", "initial", LocalDateTime.now());
        final Agenda agenda = new Agenda(project, user).bootstrap();
        final int total = 10;
        for (int num = 1; num < total; ++num) {
            agenda.add(
                String.format("gh:test/test#%d", num),
                "QA",
                String.format("https://test/%d", num)
            );
        }
        MatcherAssert.assertThat(
            new VsNoRoom(project).take(user, new StringBuilder(0)),
            Matchers.equalTo(0.0)
        );
    }
}
