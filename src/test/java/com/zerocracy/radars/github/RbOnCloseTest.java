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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import com.zerocracy.jstk.farm.fake.FkFarm;
import java.io.IOException;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link RbOnClose}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 * @checkstyle LineLength (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
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
        storage.apply(
            new Directives()
                .xpath(
                    String.format(
                        "/github/repos/repo[@coords='%s']/issue-events/issue-event[issue='%d' and event='closed']/login",
                        repo.coordinates(),
                        issue.number()
                    )
                ).set("rultor")
        );
        MatcherAssert.assertThat(
            "issue wasn't closed",
            new RbOnClose().react(
                new FkFarm(),
                github,
                Json.createObjectBuilder()
                    .add(
                        "issue",
                        Json.createObjectBuilder().add(
                            "number",
                            issue.number()
                        )
                    ).add(
                    "repository",
                    Json.createObjectBuilder().add(
                        "full_name",
                        repo.coordinates().toString()
                    )
                ).build()
            ),
            Matchers.startsWith("Asked WBS")
        );
    }
}
