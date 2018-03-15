/**
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

import com.jcabi.aspects.Tv;
import com.jcabi.github.Issue;
import com.jcabi.github.Milestone;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.radars.github.Job;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link RnkMilestone}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.22
 */
public final class RnkMilestoneTest {
    /**
     * RnkMilestone gives higher rank to issues with milestone
     * with earlier date.
     * @throws Exception If fails
     * @todo #575:30min MkIssue milestone() is not working properly,
     *  Github uses different API for fetch and edit issues:
     *  https://developer.github.com/v3/issues/#edit-an-issue
     *  it read issue milestone as a json-object but update it as a number.
     *  It should be fixed in jcabi-github then this test can be un-ignored.
     */
    @Test
    @Ignore
    public void giveHigherRankToEarlierDate() throws Exception {
        final MkGithub github = new MkGithub();
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("repos", false)
        );
        final Milestone release = repo.milestones().create("Release");
        final Milestone pre = repo.milestones().create("PreRelease");
        new Milestone.Smart(release).dueOn(new Date((long) Tv.HUNDRED));
        new Milestone.Smart(pre).dueOn(new Date((long) Tv.FIFTY));
        final Issue first = repo.issues().create("First", "");
        final Issue second = repo.issues().create("Second", "");
        final Issue third = repo.issues().create("Third", "");
        new Issue.Smart(first).milestone(pre);
        new Issue.Smart(second).milestone(release);
        new Issue.Smart(third).milestone(release);
        final List<String> jobs = new ArrayList<>(3);
        jobs.add(new Job(third).toString());
        jobs.add(new Job(first).toString());
        jobs.add(new Job(second).toString());
        jobs.sort(new RnkMilestone(github));
        MatcherAssert.assertThat(
            jobs.get(0),
            Matchers.equalTo(new Job(first).toString())
        );
    }
}
