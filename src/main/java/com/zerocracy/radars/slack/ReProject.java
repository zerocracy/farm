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
package com.zerocracy.radars.slack;

import com.jcabi.xml.XMLDocument;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.Claims;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.radars.ClaimOnQuestion;
import com.zerocracy.radars.Question;
import java.io.IOException;

/**
 * Project reaction.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReProject implements Reaction<SlackMessagePosted> {

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        final Question question = new Question(
            new XMLDocument(this.getClass().getResource("q-project.xml")),
            event.getMessageContent().split("\\s+", 2)[1].trim()
        );
        final Project project = ReProject.project(question, farm, event);
        try (final Claims claims = new Claims(project).lock()) {
            claims.add(
                new ClaimOnQuestion(question)
                    .claim()
                    .token(new SkToken(event))
                    .author(new SkPerson(farm, event).uid())
                    .param("project", project)
            );
        }
        return question.matches();
    }

    /**
     * Create project.
     * @param question Question
     * @param farm Farm
     * @param event Event
     * @return Project
     */
    private static Project project(final Question question, final Farm farm,
        final SlackMessagePosted event) {
        final Project project;
        if (question.matches() && question.code().startsWith("pm.")) {
            project = new SkProject(farm, event);
        } else {
            project = new Pmo(farm);
        }
        return project;
    }

}
