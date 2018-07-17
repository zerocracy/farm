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
import com.zerocracy.pm.staff.Elections;
import com.zerocracy.pm.staff.Votes;
import org.cactoos.Proc;
import org.cactoos.iterable.RangeOf;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsOverElected}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VsOverElectedTest {
    /**
     * Fake votes.
     */
    private static final Votes VS_FAKE = (login, log) -> 1.0;

    @Test
    public void voteForOverElected() throws Exception {
        final int max = 3;
        final FkProject pkt = new FkProject();
        final String login = "user1";
        new VsOverElectedTest.Elect(pkt, max + 1).exec(login);
        MatcherAssert.assertThat(
            new VsOverElected(
                pkt,
                new FkFarm()
            ).take(login, new StringBuilder()),
            Matchers.closeTo(1.0, 0.001)
        );
    }

    @Test
    public void ignoreForNotOverElected() throws Exception {
        final int max = 3;
        final FkProject pkt = new FkProject();
        final String login = "user2";
        new VsOverElectedTest.Elect(pkt, max).exec(login);
        MatcherAssert.assertThat(
            new VsOverElected(
                pkt,
                new FkFarm()
            ).take(login, new StringBuilder()),
            Matchers.closeTo(0.0, 0.001)
        );
    }

    @Test
    public void zeroVoteForEmptyElection() throws Exception {
        final FkProject pkt = new FkProject();
        final Elections elections = new Elections(pkt).bootstrap();
        final String login = "newcomer";
        final String job = "gh:test/test#1";
        elections.elect(
            job,
            new ListOf<>(login),
            new MapOf<Votes, Integer>(
                new MapEntry<>((lgn, jbb) -> -100.0, 1)
            )
        );
        MatcherAssert.assertThat(
            new VsOverElected(
                pkt,
                new FkFarm()
            ).take(login, new StringBuilder()),
            Matchers.closeTo(0.0, 0.001)
        );
    }

    /**
     * Elect a performer multiple times.
     */
    private static final class Elect implements Proc<String> {

        /**
         * Project.
         */
        private final Project pkt;

        /**
         * Times to elect.
         */
        private final int cnt;

        /**
         * Ctor.
         *
         * @param project Project
         * @param count Times to elect
         */
        private Elect(final Project project, final int count) {
            this.pkt = project;
            this.cnt = count;
        }

        @Override
        public void exec(final String login) throws Exception {
            final Elections elections = new Elections(this.pkt).bootstrap();
            new And(
                (Integer num) -> elections.elect(
                    String.format("gh:test/test#%d", num),
                    new ListOf<>(login),
                    new MapOf<Votes, Integer>(
                        new MapEntry<>(VsOverElectedTest.VS_FAKE, 1)
                    )
                ),
                new RangeOf<>(1, this.cnt, num -> num + 1)
            ).value();
        }
    }
}
