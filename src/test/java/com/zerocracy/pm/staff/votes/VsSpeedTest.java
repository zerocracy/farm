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

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Speed;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsSpeed}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumber (500 line)
 */
public final class VsSpeedTest {
    @Test
    public void giveHigherVoteForFastSpeed() throws Exception {
        final String one = "user2";
        final String two = "user3";
        final FkFarm farm = new FkFarm();
        new Speed(farm, one).bootstrap().add(
            "TST000002",
            "gh:test/test#2",
            TimeUnit.DAYS.toMinutes(9L)
        );
        new Speed(farm, two).bootstrap().add(
            "TST000001",
            "gh:test/test#22",
            TimeUnit.DAYS.toMinutes(8L)
        );
        final VsSpeed votes = new VsSpeed(
            new Pmo(farm),
            new ListOf<>(one, two)
        );
        MatcherAssert.assertThat(
            votes.take(two, new StringBuilder(0)),
            Matchers.greaterThan(votes.take(one, new StringBuilder(0)))
        );
    }

    @Test
    public void giveZeroSpeedLowestVote() throws Exception {
        final String one = "user_one";
        final String two = "user_two";
        final FkFarm farm = new FkFarm();
        new Speed(farm, two).bootstrap().add(
            "TST000003",
            "gh:test/test#3",
            TimeUnit.DAYS.toMinutes(1L)
        );
        final VsSpeed votes = new VsSpeed(
            new Pmo(farm),
            new ListOf<>(one, two)
        );
        MatcherAssert.assertThat(
            votes.take(one, new StringBuilder(0)),
            Matchers.lessThan(votes.take(two, new StringBuilder(0)))
        );
    }
}
