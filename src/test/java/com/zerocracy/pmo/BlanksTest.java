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
import java.time.Duration;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Blanks}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class BlanksTest {

    /**
     * Issue blank kind.
     */
    private static final String ISSUE = "issue";

    @Test
    public void addBlank() throws Exception {
        final Blanks blanks = new Blanks(new FkFarm(), "user1")
            .bootstrap();
        blanks.add(new FkProject(), "gh:test/test#1", BlanksTest.ISSUE);
        blanks.add(new FkProject(), "gh:test/test#2", "pull-request");
        MatcherAssert.assertThat(
            blanks.iterate(),
            Matchers.iterableWithSize(2)
        );
    }

    @Test
    public void calcualtesCount() throws Exception {
        final Blanks blanks = new Blanks(new FkFarm(), "user2").bootstrap();
        MatcherAssert.assertThat(
            "Empty blanks",
            blanks.total(), Matchers.equalTo(0)
        );
        blanks.add(new FkProject(), "gh:test/test#3", BlanksTest.ISSUE);
        MatcherAssert.assertThat(
            "Single blank",
            blanks.total(), Matchers.equalTo(1)
        );
        blanks.add(new FkProject(), "gh:test/test#4", BlanksTest.ISSUE);
        MatcherAssert.assertThat(
            "Two blanks",
            blanks.total(), Matchers.equalTo(2)
        );
    }

    @Test
    public void overrideBlankOnUpdate() throws Exception {
        final Blanks blanks = new Blanks(new FkFarm(), "user3").bootstrap();
        final Project pkt = new FkProject();
        final String job = "gh:test/test#10";
        blanks.add(pkt, job, BlanksTest.ISSUE);
        blanks.add(pkt, job, BlanksTest.ISSUE);
        MatcherAssert.assertThat(
            blanks.total(), Matchers.equalTo(1)
        );
    }

    @Test
    public void removeOldBlanks() throws Exception {
        final Blanks blanks = new Blanks(new FkFarm(), "user4").bootstrap();
        final Project pkt = new FkProject();
        final String job = "gh:test/test#11";
        final Instant time = Instant.ofEpochMilli(1563956761000L);
        blanks.add(pkt, job, BlanksTest.ISSUE, time);
        // @checkstyle MagicNumberCheck (1 line)
        blanks.removeOlderThan(time.plus(Duration.ofDays(91L)));
        MatcherAssert.assertThat(blanks.total(), Matchers.is(0));
    }
}
