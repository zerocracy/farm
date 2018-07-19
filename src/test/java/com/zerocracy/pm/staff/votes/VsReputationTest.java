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
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.Pmo;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsReputation}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class VsReputationTest {
    @Test
    public void givesHigherVoteForHigherRep() throws Exception {
        final String high = "amihaiemil";
        final String low = "carlosmiranda";
        final FkFarm farm = new FkFarm();
        final Project project = new FkProject();
        new Awards(farm, high).bootstrap().add(
            project,
            Tv.THOUSAND,
            "gh:test/test#1",
            "test high"
        );
        new Awards(farm, low).bootstrap().add(
            project,
            Tv.HUNDRED,
            "gh:test/test#2",
            "test low"
        );
        final VsReputation votes =
            new VsReputation(new Pmo(farm), new ListOf<>(high, low));
        MatcherAssert.assertThat(
            votes.take(high, new StringBuilder()),
            Matchers.greaterThan(votes.take(low, new StringBuilder()))
        );
    }
}
