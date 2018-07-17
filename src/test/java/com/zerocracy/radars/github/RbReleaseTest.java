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
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.PropsFarm;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RbRelease}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RbReleaseTest {
    @Test
    public void acceptsRelease() throws Exception {
        final int id = 1;
        final String tag = "0.0.1";
        final Farm farm = new PropsFarm();
        final Github github = new MkGithub().relogin("test");
        github.repos().create(new Repos.RepoCreate("one", false));
        MatcherAssert.assertThat(
            new RbRelease().react(
                farm,
                github,
                Json.createObjectBuilder()
                    .add(
                        "release",
                        Json.createObjectBuilder()
                            .add("tag_name", tag)
                            .add("id", id)
                            .add("published_at", "2018-04-06T07:00:00Z")
                    )
                    .add(
                        "repository",
                        Json.createObjectBuilder()
                            .add("full_name", "test/one")
                    ).build()
            ),
            Matchers.is(
                String.format(
                    "Release published: %d (tag: %s)", id, tag
                )
            )
        );
    }
}
