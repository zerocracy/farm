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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pm.staff.Votes;
import com.zerocracy.pmo.Blanks;
import com.zerocracy.pmo.Pmo;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsBlanks}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsBlanksTest {

    @Test
    public void giveHigherVoteWhenMoreBlanks() throws Exception {
        final String worse = "user2";
        final String better = "user3";
        final FkFarm farm = new FkFarm();
        final FkProject pkt = new FkProject();
        final String kind = "issue";
        new Blanks(farm, worse).bootstrap().add(
            pkt, "gh:test/test#2", kind
        );
        new Blanks(farm, worse).bootstrap().add(
            pkt, "gh:test/test#3", kind
        );
        new Blanks(farm, better).bootstrap().add(
            pkt, "gh:test/test#22", kind
        );
        final Votes votes = new VsBlanks(
            new Pmo(farm),
            new ListOf<>(worse, better)
        );
        MatcherAssert.assertThat(
            votes.take(better, new StringBuilder(0)),
            Matchers.lessThan(votes.take(worse, new StringBuilder(0)))
        );
    }
}
