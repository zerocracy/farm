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
package com.zerocracy.crews.github;

import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.qa.Question;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Stakeholder by command.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkReaction implements Stakeholder {

    /**
     * GitHub event.
     */
    private final Event event;

    /**
     * Commands and reactions.
     */
    private final Map<String, Reaction> routes;

    /**
     * Ctor.
     * @param evt Event in GitHub
     * @param map Routes map
     */
    public StkReaction(final Event evt, final Map<String, Reaction> map) {
        this.event = evt;
        this.routes = map;
    }

    @Override
    public void work() throws IOException {
        final Comment.Smart comment = new Comment.Smart(this.event.comment());
        final Question question = new Question(
            comment.body().split(" ", 2)[1], this.routes.keySet()
        );
        comment.issue().comments().post(
            String.format(
                "> %s%n%n@%s %s",
                StringUtils.abbreviate(comment.body(), Tv.FIFTY)
                    .replaceAll("\n", " "),
                comment.author().login(),
                this.routes.get(question.option().group())
                    .answer(this.event, question)
            )
        );
    }
}
