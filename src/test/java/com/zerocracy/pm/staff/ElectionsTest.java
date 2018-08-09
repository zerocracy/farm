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
package com.zerocracy.pm.staff;

import com.zerocracy.Farm;
import com.zerocracy.RunsInThreads;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.spy.SpyProject;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pmo.Pmo;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.collection.Filtered;
import org.cactoos.list.SolidList;
import org.cactoos.list.StickyList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.map.StickyMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Elections}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ElectionsTest {

    @Test
    public void voteAndBuildReason() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#1";
        elections.elect(
            job,
            new SolidList<>("yegor256", "jeff"),
            new StickyMap<Votes, Integer>(
                new MapEntry<>(
                    (login, log) -> {
                        log.append("just some log");
                        return new SecureRandom().nextDouble();
                    },
                    1
                ),
                new MapEntry<>(
                    (login, log) -> {
                        log.append("just some other log");
                        return new SecureRandom().nextDouble();
                    },
                    2
                )
            )
        );
        MatcherAssert.assertThat(
            elections.result(job).reason(),
            Matchers.containsString("@yegor256 (")
        );
    }

    @Test
    public void votesAndPicksWinner() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#2";
        elections.elect(
            job,
            new SolidList<>("loser", "loser2", "win", "yegor"),
            new StickyMap<Votes, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d / (double) login.length(),
                    1
                )
            )
        );
        final ElectionResult result = elections.result(job);
        MatcherAssert.assertThat(result.elected(), Matchers.is(true));
        MatcherAssert.assertThat(result.winner(), Matchers.startsWith("wi"));
    }

    @Test
    public void removesElection() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#3";
        elections.elect(
            job,
            new SolidList<>("myfriend"),
            new StickyMap<Votes, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d / (double) login.length(),
                    1
                )
            )
        );
        elections.remove(job);
        MatcherAssert.assertThat(
            elections.result(job).elected(),
            Matchers.is(false)
        );
    }

    @Test
    public void doesntElectWithNegativeScore() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#55";
        elections.elect(
            job,
            new SolidList<>("somebody"),
            new StickyMap<Votes, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d,
                    -1
                )
            )
        );
        MatcherAssert.assertThat(
            elections.result(job).elected(),
            Matchers.is(false)
        );
    }

    @Test
    public void calculatesAge() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#102";
        elections.elect(
            job,
            new SolidList<>("david"),
            new StickyMap<Votes, Integer>(
                new MapEntry<>((login, log) -> 1.0d, -1)
            )
        );
        MatcherAssert.assertThat(elections.age(), Matchers.greaterThan(0L));
    }

    @Test
    public void modifiesItemOnlyOnce() throws Exception {
        final Collection<String> ops = new LinkedList<>();
        final Elections elections = new Elections(
            new SpyProject(
                new FkProject(),
                ops::add
            )
        ).bootstrap();
        final String job = "gh:test/test#92";
        // @checkstyle DiamondOperatorCheck (1 line)
        final Map<Votes, Integer> voters = new SolidMap<Votes, Integer>(
            new MapEntry<>((login, log) -> 1.0d, -1)
        );
        final Iterable<String> logins = new StickyList<>("james");
        MatcherAssert.assertThat(
            elections.elect(job, logins, voters),
            Matchers.is(true)
        );
        // @checkstyle MagicNumberCheck (1 line)
        for (int idx = 0; idx < 20; ++idx) {
            MatcherAssert.assertThat(
                elections.elect(job, logins, voters),
                Matchers.is(false)
            );
        }
        MatcherAssert.assertThat(
            new Filtered<>(
                op -> op.startsWith("update:"), ops
            ).size(),
            Matchers.equalTo(2)
        );
    }

    @Test
    public void electsOnlyOnce() throws Exception {
        try (final Farm farm = new SyncFarm(new FkFarm())) {
            final Elections elections = new Elections(
                new Pmo(farm)
            ).bootstrap();
            final String job = "gh:test/test#550";
            final Iterable<String> users = new SolidList<>("alex", "alex2");
            // @checkstyle DiamondOperatorCheck (1 line)
            final Map<Votes, Integer> voters = new SolidMap<Votes, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d / (double) login.length(),
                    1
                )
            );
            final AtomicInteger elected = new AtomicInteger();
            MatcherAssert.assertThat(
                inc -> {
                    if (elections.elect(job, users, voters)) {
                        elected.incrementAndGet();
                    }
                    return true;
                },
                new RunsInThreads<>()
            );
            MatcherAssert.assertThat(elected.get(), Matchers.equalTo(1));
        }
    }

}
