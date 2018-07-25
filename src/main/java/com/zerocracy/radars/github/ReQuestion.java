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
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.radars.ClaimOnQuestion;
import com.zerocracy.radars.Question;
import java.io.IOException;

/**
 * Parse and answer the question.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReQuestion implements Response {

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        final String[] parts = comment.body().trim().split("\\s+", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException(
                String.format("Wrong input: \"%s\"", comment.body())
            );
        }
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource(
                    "/com/zerocracy/radars/q-tracker.xml"
                )
            ),
            parts[1].trim()
        );
        final Project project = new GhProject(farm, comment);
        new ClaimOnQuestion(question)
            .claim()
            .token(new TokenOfComment(comment))
            .author(new GhUser(farm, comment.author()).uid(question.invited()))
            .param("job", new Job(comment.issue()))
            .param("repo", comment.issue().repo().coordinates())
            .param("issue", comment.issue().number())
            .param("comment", comment.number())
            .postTo(new ClaimsOf(farm, project));
        return question.matches();
    }

}
