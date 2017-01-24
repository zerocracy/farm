/**
 * Copyright (c) 2016 Zerocracy
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

import com.google.common.collect.Iterables;
import com.jcabi.github.Comment;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.radars.ClaimOnQuestion;
import com.zerocracy.radars.Question;
import java.io.IOException;

/**
 * Parse and answer the question.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReQuestion implements Response {

    @Override
    public boolean react(final Farm farm, final Comment.Smart comment)
        throws IOException {
        final Question question = new Question(
            new XMLDocument(this.getClass().getResource("q-github.xml")),
            comment.body().split("\\s+", 2)[1].trim()
        );
        final Project project = new GhProject(farm, comment);
        try (final Claims claims = new Claims(project).lock()) {
            claims.add(
                Iterables.concat(
                    new ClaimOut()
                        .token(new Token(comment))
                        .author(new Author(comment.author()))
                        .param("job", new Job(comment.issue())),
                    new ClaimOnQuestion(question)
                )
            );
        }
        return question.matches();
    }

}
