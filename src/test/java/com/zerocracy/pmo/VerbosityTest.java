/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.pmo;

import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Verbosity}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VerbosityTest {

    @Test
    public void addsVerbosity() throws Exception {
        final String job = "gh:test/test#1";
        final int value = 1;
        final Verbosity verbosity =
            new Verbosity(new Pmo(new FkFarm()), "user1234").bootstrap();
        verbosity.add(new FkProject(), job, value);
        MatcherAssert.assertThat(
            verbosity.messages(),
            Matchers.equalTo(value)
        );
    }

    @Test
    public void overridesVerbosity() throws IOException {
        final Pmo pmo = new Pmo(new FkFarm());
        final FkProject pkt = new FkProject();
        final String login = "paulodamaso";
        final String job = "gh:test/test#256";
        final int newvalue = 5;
        final Verbosity verbosity = new Verbosity(pmo, login).bootstrap();
        verbosity.add(pkt, job, 1);
        verbosity.add(pkt, job, newvalue);
        MatcherAssert.assertThat(
            verbosity.messages(),
            Matchers.equalTo(newvalue)
        );
    }

    @Test
    public void removeOldItems() throws Exception {
        final Project pkt = new FkProject();
        final Verbosity verbosity = new Verbosity(new FkFarm(), "user4")
            .bootstrap();
        final String job = "gh:test/test#11";
        final Instant time = Instant.ofEpochMilli(1563956761000L);
        verbosity.add(pkt, job, 1, time);
        // @checkstyle MagicNumberCheck (1 line)
        verbosity.removeOlderThan(time.plus(Duration.ofDays(91L)));
        MatcherAssert.assertThat(verbosity.messages(), Matchers.is(0));
    }
}
