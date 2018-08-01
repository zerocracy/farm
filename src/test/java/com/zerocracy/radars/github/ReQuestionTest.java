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
import com.zerocracy.Farm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.People;
import org.junit.Test;

/**
 * Test case for {@link ReQuestion}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ReQuestionTest {

    @Test
    public void parsesQuestion() throws Exception {
        final String uid = "jeff";
        final Github github = new MkGithub(uid);
        final Repo repo = github.repos().create(
            new Repos.RepoCreate("test", false)
        );
        final Issue issue = new Issue.Smart(
            repo.issues().create("first title", "")
        );
        final Comment comment = issue.comments().post("@0crat hello");
        final Farm farm = new PropsFarm();
        new People(farm).bootstrap().invite(uid, "yegor256");
        new ReQuestion().react(farm, new Comment.Smart(comment));
    }
}
