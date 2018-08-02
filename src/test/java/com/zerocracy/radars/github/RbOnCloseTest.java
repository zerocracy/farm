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

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.scope.Wbs;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Test case for {@link RbOnClose}.
 * @since 1.0
 * @checkstyle LineLength (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RbOnCloseTest {

    @Test
    public void closedByAny() throws IOException {
        final MkStorage storage = new MkStorage.InFile();
        final Github github = new MkGithub(storage, "g4s8");
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("farm", false)
        );
        final Issue.Smart issue = new Issue.Smart(
            repo.issues().create("test", "")
        );
        issue.close();
        storage.apply(RbOnCloseTest.closeEvent(issue));
        MatcherAssert.assertThat(
            "issue wasn't closed",
            new RbOnClose().react(new PropsFarm(), github, RbOnCloseTest.json(issue)),
            Matchers.startsWith("Asked WBS")
        );
    }

    @Test
    public void registerDelayedClaim() throws IOException {
        final MkStorage storage = new MkStorage.InFile();
        final Github github = new MkGithub(storage, "user");
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("datum", false)
        );
        final Issue.Smart issue = new Issue.Smart(
            repo.issues().create("bug", "")
        );
        issue.close();
        storage.apply(RbOnCloseTest.closeEvent(issue));
        final Farm farm = new PropsFarm();
        final GhProject pkt = new GhProject(farm, repo);
        new Wbs(pkt).bootstrap().add(new Job(issue).toString());
        new RbOnClose().react(farm, github, RbOnCloseTest.json(issue));
        MatcherAssert.assertThat(
            "issue is not delayed",
            new ClaimsItem(pkt).bootstrap().iterate(),
            Matchers.emptyIterable()
        );
    }

    private static JsonObject json(final Issue issue) {
        return Json.createObjectBuilder()
            .add(
                "issue",
                Json.createObjectBuilder().add(
                    "number",
                    issue.number()
                )
            )
            .add(
                "repository",
                Json.createObjectBuilder().add(
                    "full_name",
                    issue.repo().coordinates().toString()
                )
            )
            .add(
                "sender",
                Json.createObjectBuilder().add("login", "yegor")
            )
            .build();
    }

    private static Iterable<Directive> closeEvent(final Issue issue) {
        return new Directives()
            .xpath(
                String.format(
                    "/github/repos/repo[@coords='%s']/issue-events/issue-event[issue='%d' and event='closed']/login",
                    issue.repo().coordinates(),
                    issue.number()
                )
            ).set("rultor");
    }
}
