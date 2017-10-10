/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.zerocracy.jstk.farm.fake.FkProject;
import com.zerocracy.jstk.farm.spy.SpyProject;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import org.cactoos.collection.Filtered;
import org.cactoos.list.StickyList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.StickyMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Elections}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
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
            new StickyList<>("yegor256", "jeff"),
            new StickyMap<Voter, Integer>(
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
            elections.reason(job),
            Matchers.containsString("@yegor256 (")
        );
    }

    @Test
    public void votesAndPicksWinner() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#2";
        elections.elect(
            job,
            new StickyList<>("loser", "loser2", "win", "yegor"),
            new StickyMap<Voter, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d / (double) login.length(),
                    1
                )
            )
        );
        MatcherAssert.assertThat(elections.elected(job), Matchers.is(true));
        MatcherAssert.assertThat(
            elections.winner(job),
            Matchers.startsWith("wi")
        );
    }

    @Test
    public void removesElection() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#3";
        elections.elect(
            job,
            new StickyList<>("myfriend"),
            new StickyMap<Voter, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d / (double) login.length(),
                    1
                )
            )
        );
        elections.remove(job);
        MatcherAssert.assertThat(elections.elected(job), Matchers.is(false));
    }

    @Test
    public void doesntElectWithNegativeScore() throws Exception {
        final Elections elections = new Elections(new FkProject()).bootstrap();
        final String job = "gh:test/test#55";
        elections.elect(
            job,
            new StickyList<>("somebody"),
            new StickyMap<Voter, Integer>(
                new MapEntry<>(
                    (login, log) -> 1.0d,
                    -1
                )
            )
        );
        MatcherAssert.assertThat(elections.elected(job), Matchers.is(false));
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
        final Map<Voter, Integer> voters = new StickyMap<Voter, Integer>(
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
                ops, op -> op.startsWith("update:")
            ).size(),
            Matchers.equalTo(2)
        );
    }

}
