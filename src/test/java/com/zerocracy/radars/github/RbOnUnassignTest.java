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
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.farm.fake.FkFarm;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RbOnUnassign}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbOnUnassignTest {

    @Test
    public void unassigned() throws Exception {
        final MkGithub github = new MkGithub();
        final Repo repo = github.repos()
            .create(new Repos.RepoCreate("test", false));
        final Issue issue = repo.issues().create("bug", "hello");
        final String username = "user11";
        new Issue.Smart(issue).assign(username);
        MatcherAssert.assertThat(
            "Failed to unassign",
            new RbOnUnassign().react(
                new FkFarm(),
                github,
                Json.createObjectBuilder()
                    .add("action", "unassigned")
                    .add(
                        "sender",
                        Json.createObjectBuilder()
                            .add("login", "yegor256")
                    )
                    .add(
                        "issue",
                        Json.createObjectBuilder()
                            .add("number", issue.number())
                    )
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", repo.coordinates().toString())
                    )
                    .add(
                        "assignee",
                        Json.createObjectBuilder()
                            .add("login", username)
                    ).build()
            ),
            Matchers.containsString("Issue #1 was unassigned")
        );
    }
}
