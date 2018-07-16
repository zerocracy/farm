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

import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.github.mock.MkStorage;
import com.zerocracy.Farm;
import com.zerocracy.SoftException;
import com.zerocracy.farm.fake.FkFarm;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link ReIfAddressed}.
 * @since 1.0
 */
public final class ReIfAddressedTest {

    /**
     * ReIfAddressed delegates the addressed comment to the encapsulated
     * Response.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void delegatesAddressedComment() throws IOException {
        final Comment.Smart initial = this.comment("@0crat hello");
        final Farm fake = new FkFarm();
        final String response = "@jeff hello to you, too!";
        final ReIfAddressed ria = new ReIfAddressed(
            (farm, comment) -> {
                MatcherAssert.assertThat(
                    fake == farm, Matchers.is(Boolean.TRUE)
                );
                MatcherAssert.assertThat(
                    comment.body(),
                    Matchers.equalTo(initial.body())
                );
                comment.issue().comments().post(response);
                return true;
            }
        );
        ria.react(fake, initial);
        MatcherAssert.assertThat(
            "Initial comment was not responded to!",
            new Comment.Smart(initial.issue().comments().get(2)).body(),
            Matchers.equalTo(response)
        );
    }

    /**
     * ReIfAddressed delegates the addressed comment to the encapsulated
     * Response.
     * @throws IOException If something goes wrong.
     */
    @Test
    public void ignoresInnocentComments() throws IOException {
        final Comment.Smart initial = this.comment("How are you sir?");
        final ReIfAddressed ria = new ReIfAddressed(
            (farm, comment) -> {
                Assert.fail("shouldn't happen");
                return true;
            }
        );
        final Farm fake = new FkFarm();
        MatcherAssert.assertThat(
            ria.react(fake, initial),
            Matchers.equalTo(false)
        );
    }

    /**
     * ReIfAddressed throws a SoftException if the comment is addressed but
     * containing no other text.
     * @throws IOException If something goes wrong.
     */
    @Test(expected = SoftException.class)
    public void complainsAboutEmptyComment() throws IOException {
        final Comment.Smart initial = this.comment("@0crat  ");
        final Farm fake = new FkFarm();
        final ReIfAddressed ria = new ReIfAddressed(
            (farm, comment) -> {
                Assert.fail("SoftException was expected");
                return true;
            }
        );
        ria.react(fake, initial);
    }

    /**
     * Mock a Comment for test. Jeff leaves a comment, then 0crat
     * logs in and reads it.
     * @param body The comment's body.
     * @return Github comment received by 0crat.
     * @throws IOException If something goes wrong.
     */
    private Comment.Smart comment(final String body) throws IOException {
        final MkStorage server = new MkStorage.InFile();
        final Github jeff = new MkGithub(server,  "jeff");
        final Repo repo = jeff.repos().create(
            new Repos.RepoCreate("test", false)
        );
        final Issue issue = new Issue.Smart(
            repo.issues().create("first title", "")
        );
        final Comment.Smart initial = new Comment.Smart(
            issue.comments().post(body)
        );
        return new Comment.Smart(
            new MkGithub(server,  "0crat")
                .repos().get(repo.coordinates())
                .issues().get(issue.number())
                .comments().get(initial.number())
        );
    }

}
