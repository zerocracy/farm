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
package com.zerocracy.farm;

import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGithub;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link DyErrors}.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.20
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class DyErrorsITCase {
    @Test
    public void fetchItems() throws Exception {
        final MkGithub github = new MkGithub();
        final DyErrors.Github errors = new DyErrors.Github(
            new DyErrors(
                new Region.Simple(
                    new Credentials.Direct(
                        Credentials.TEST,
                        Integer.valueOf(
                            System.getProperty("dynamo.port")
                        )
                    )
                )
            ),
            github
        );
        final Repo repo = github.repos()
            .create(new Repos.RepoCreate("test", false));
        final Issue issue = repo.issues()
            .create("A bug", "RuntimeException in main()");
        final Comment comment = issue.comments().post("error");
        Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
        final Comment deleted = issue.comments().post("to-delete");
        errors.add(comment);
        errors.add(deleted);
        errors.remove(deleted);
        MatcherAssert.assertThat(
            "Error comment was not found",
            errors.iterate(2, 0L),
            Matchers.not(Matchers.emptyIterable())
        );
    }
}
