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
package com.zerocracy.pm.staff.ranks;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.IssueLabels;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.radars.github.Job;
import java.util.ArrayList;
import java.util.List;
import org.cactoos.func.StickyBiFunc;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RnkGithubBug}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RnkGithubBugTest {
    @Test
    public void sortBugsFirst() throws Exception {
        final Github github = new MkGithub().relogin("test");
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("bugs", false)
        );
        final List<String> jobs = new ArrayList<>(3);
        jobs.add(new Job(repo.issues().create("A feature 1", "")).toString());
        final Issue bug = repo.issues().create("A bug", "");
        new IssueLabels.Smart(bug.labels()).addIfAbsent("bug");
        jobs.add(new Job(bug).toString());
        jobs.add(new Job(repo.issues().create("A feature 2", "")).toString());
        jobs.sort(new RnkGithubBug(github));
        MatcherAssert.assertThat(
            jobs,
            Matchers.contains(
                "gh:test/bugs#2",
                "gh:test/bugs#1",
                "gh:test/bugs#3"
            )
        );
    }

    @Test
    public void evaluatesFromCache() throws Exception {
        final String issue = "gh:test/cached#4";
        final String bug = "gh:test/cached#5";
        final StickyBiFunc<Github, String, Boolean> cache = new StickyBiFunc<>(
            (ghb, job) -> job.equals(bug)
        );
        final RnkGithubBug rnk = new RnkGithubBug(new MkGithub(), cache);
        MatcherAssert.assertThat(
            rnk.compare(issue, bug),
            Matchers.greaterThan(0)
        );
        MatcherAssert.assertThat(
            rnk.compare(bug, issue),
            Matchers.lessThan(0)
        );
    }
}
