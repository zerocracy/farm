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
package com.zerocracy.radars.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Milestone;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import java.util.Collection;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RbAddToMilestone}.
 * @since 0.26
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbAddToMilestoneTest {

    @Test
    public void respondsToMilestonedEvent() throws IOException {
        final MkGithub github = new MkGithub();
        final Repo repo = github.repos()
            .create(new Repos.RepoCreate("test", false));
        final Issue bug = repo
            .issues().create("bug", "");
        final Milestone milestone =
            repo.milestones().create("my milestone");
        new Issue.Smart(bug).milestone(milestone);
        final Farm farm = new PropsFarm();
        MatcherAssert.assertThat(
            new RbAddToMilestone().react(
                farm,
                github,
                Json.createObjectBuilder()
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", repo.coordinates().toString())
                            .build()
                    )
                    .add(
                        "issue",
                        Json.createObjectBuilder()
                            .add("number", 1)
                            .add(
                                "milestone",
                                Json.createObjectBuilder()
                                    .add("title", "milestone-title")
                                    .add("number", 1)
                                    .build()
                            ).build()
                    ).build()
            ),
            Matchers.is(
                String.format(
                    "Issue #%d has been added to milestone #%d",
                    bug.number(), milestone.number()
                )
            )
        );
        final Collection<XML> claims =
            new ClaimsItem(new GhProject(farm, bug.repo()))
                .bootstrap()
                .iterate();
        final ClaimIn claim = new ClaimIn(claims.iterator().next());
        MatcherAssert.assertThat(
            claim.type(),
            Matchers.is("Job milestoned")
        );
        MatcherAssert.assertThat(
            claim.param("milestone"),
            Matchers.is(String.valueOf(milestone.number()))
        );
        MatcherAssert.assertThat(
            claim.param("job"),
            Matchers.is(new Job(bug).toString())
        );
    }
}
