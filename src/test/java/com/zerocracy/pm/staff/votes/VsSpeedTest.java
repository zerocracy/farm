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

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.pmo.Speed;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsSpeed}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumber (500 line)
 */
public final class VsSpeedTest {
    @Test
    public void giveHigherVoteForFastSpeed() throws Exception {
        final String slow = "user2";
        final String fast = "user3";
        final FkFarm farm = new FkFarm();
        new Speed(farm, slow).bootstrap().add(
            "TST000002",
            "gh:test/test#2",
            TimeUnit.DAYS.toMinutes(9L),
            Instant.now()
        );
        new Speed(farm, fast).bootstrap().add(
            "TST000001",
            "gh:test/test#22",
            TimeUnit.DAYS.toMinutes(8L),
            Instant.now()
        );
        final VsSpeed votes = new VsSpeed(
            new Pmo(farm),
            new ListOf<>(slow, fast)
        );
        MatcherAssert.assertThat(
            votes.take(fast, new StringBuilder(0)),
            Matchers.greaterThan(votes.take(slow, new StringBuilder(0)))
        );
    }

    @Test
    public void giveLowestVoteForUnknownSpeed() throws Exception {
        final String unknown = "user_one";
        final String known = "user_two";
        final FkFarm farm = new FkFarm();
        new Speed(farm, known).bootstrap().add(
            "TST000003",
            "gh:test/test#3",
            TimeUnit.DAYS.toMinutes(1L),
            Instant.now()
        );
        final VsSpeed votes = new VsSpeed(
            new Pmo(farm),
            new ListOf<>(unknown, known)
        );
        MatcherAssert.assertThat(
            votes.take(unknown, new StringBuilder(0)),
            Matchers.lessThan(votes.take(known, new StringBuilder(0)))
        );
    }
}
