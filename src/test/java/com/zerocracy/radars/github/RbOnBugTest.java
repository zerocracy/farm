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
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import javax.json.Json;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RbOnBug}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbOnBugTest {
    @Test
    public void postAddToWbsClaim() throws Exception {
        final MkGithub github = new MkGithub();
        final Issue bug = github.repos()
            .create(new Repos.RepoCreate("datum", false))
            .issues().create("bug", "");
        MatcherAssert.assertThat(
            new RbOnBug().react(
                new PropsFarm(new FkFarm(new FkProject())),
                github,
                RbOnBugTest.payload(bug)
            ),
            Matchers.containsString("added to WBS")
        );
    }

    private static JsonObject payload(final Issue issue) {
        return Json.createObjectBuilder().add(
            "issue",
            Json.createObjectBuilder()
                .add("number", issue.number())
        ).add(
            "repository",
            Json.createObjectBuilder()
                .add(
                    "full_name",
                    issue.repo().coordinates().toString()
                )
        ).build();
    }
}
