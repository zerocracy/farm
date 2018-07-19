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

import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pm.staff.Roles;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link RbPingArchitect}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RbPingArchitectTest {

    @Test
    public void informsAboutMissingArc() throws IOException {
        final MkGithub github = new MkGithub("random");
        MatcherAssert.assertThat(
            new RbPingArchitect().react(
                new FkFarm(), github,
                RbPingArchitectTest.event(true, github)
            ),
            Matchers.is("No architects here")
        );
    }

    @Test
    public void handlesPullRequest() throws IOException {
        final MkGithub github = new MkGithub("smith");
        final FkProject pkt = new FkProject();
        final Roles roles = new Roles(pkt).bootstrap();
        roles.assign("jeff", "ARC");
        roles.assign("john", "REV");
        MatcherAssert.assertThat(
            new RbPingArchitect().react(
                new FkFarm(pkt.pid(), pkt),
                github,
                RbPingArchitectTest.event(false, github)
            ),
            Matchers.is("Some REV will pick it up")
        );
    }

    @Test
    public void handlesArcIssue() throws IOException {
        final MkGithub github = new MkGithub("jeff");
        final FkProject pkt = new FkProject();
        final FkFarm farm = new FkFarm(pkt.pid(), pkt);
        new Roles(pkt).bootstrap().assign("jeff", "ARC");
        MatcherAssert.assertThat(
            new RbPingArchitect().react(
                farm,
                github,
                RbPingArchitectTest.event(true, github)
            ),
            Matchers.is("The architect is speaking")
        );
    }

    private static JsonObject event(final boolean issue,
        final MkGithub github)
        throws IOException {
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("test", false)
        );
        final JsonObjectBuilder builder = Json.createObjectBuilder()
            .add(
                "repository",
                Json.createObjectBuilder()
                    .add("full_name", repo.coordinates().toString())
                    .build()
            );
        if (issue) {
            builder.add(
                "issue",
                Json.createObjectBuilder()
                    .add(
                        "number",
                        github.repos().get(repo.coordinates())
                            .issues()
                            .create("Some issue", "Body of issue")
                            .number()
                    ).build()
            );
        } else {
            builder.add(
                "pull_request",
                Json.createObjectBuilder()
                    .add(
                        "number",
                        github.repos().get(repo.coordinates())
                            .pulls().create("Some PR", "1", "2")
                            .number()
                    ).build()
            );
        }
        return builder.build();
    }
}
