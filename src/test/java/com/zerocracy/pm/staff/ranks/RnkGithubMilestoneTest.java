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
package com.zerocracy.pm.staff.ranks;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Milestone;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.radars.github.Job;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RnkGithubMilestone}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RnkGithubMilestoneTest {

    @Test
    public void sortMilestonedIssuesFirst() throws Exception {
        final Github github = new MkGithub().relogin("test");
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("milestones", false)
        );
        final List<String> jobs = new LinkedList<>();
        final Job first = new Job(repo.issues().create("No milestone 1", ""));
        jobs.add(first.toString());
        final Issue milestoned = repo.issues().create("Has milestone", "");
        final Milestone milestone = repo.milestones().create("1.0");
        new Issue.Smart(milestoned).milestone(milestone);
        jobs.add(new Job(milestoned).toString());
        final Job third = new Job(repo.issues().create("No milestone 2", ""));
        jobs.add(third.toString());
        jobs.sort(new RnkGithubMilestone(github));
        MatcherAssert.assertThat(
            jobs,
            Matchers.contains(
                new Job(milestoned).toString(),
                first.toString(),
                third.toString()
            )
        );
    }
}
