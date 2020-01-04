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
package com.zerocracy.pm.scope;

import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsEqual;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link Wbs}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @todo #1640:30min Implement wbs.add(job, author) and wbs.author(job) methods to change
 *  wbs.add() method behavior (every wbs item should have an author). After this, remove
 *  addsAndRemovesJobs() test and replace it by addsAndRemovesJobWithAuthor() removing its ignore
 *  annotation.
 */
public final class WbsTest {

    @Test
    public void addsAndRemovesJobs() throws Exception {
        final Wbs wbs = new Wbs(new FkProject()).bootstrap();
        final String job = "gh:yegor256/0pdd#3";
        wbs.add(job);
        MatcherAssert.assertThat(wbs.iterate(), Matchers.hasItem(job));
        wbs.remove(job);
        MatcherAssert.assertThat(
            wbs.iterate(), Matchers.not(Matchers.hasItem(job))
        );
    }

    @Test
    @Ignore
    public void addsAndRemovesJobWithAuthor() throws Exception {
        final Wbs wbs = new Wbs(new FkProject()).bootstrap();
        final String job = "gh:yegor256/0pdd#1";
        final String author = "author";
        wbs.add(job, author);
        MatcherAssert.assertThat(
            "Couldn't add job with author",
            wbs.author(job),
            new IsEqual<>(author)
        );
        wbs.remove(job);
        MatcherAssert.assertThat(
            "Couldn't remove job with author",
            wbs.iterate(),
            new IsCollectionContaining<>(
                new IsEqual<>(job)
            )
        );
    }

    @Test
    public void setsRole() throws Exception {
        final Wbs wbs = new Wbs(new FkProject()).bootstrap();
        final String job = "gh:yegor256/0pdd#99";
        wbs.add(job);
        wbs.role(job, "REV");
        MatcherAssert.assertThat(
            wbs.role(job), Matchers.not(Matchers.equalTo("DEV"))
        );
    }

    @Test
    public void calculatesSize() throws Exception {
        final Wbs wbs = new Wbs(new FkProject()).bootstrap();
        final int size = 100;
        for (int job = 0; job < size; ++job) {
            wbs.add(String.format("gh:test/test#job%d", job));
        }
        MatcherAssert.assertThat(
            wbs.size(), Matchers.equalTo(size)
        );
    }
}
