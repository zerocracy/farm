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
package com.zerocracy.radars.slack;

import com.jcabi.xml.XMLDocument;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.zerocracy.Farm;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.radars.ClaimOnQuestion;
import com.zerocracy.radars.Question;
import java.io.IOException;

/**
 * Project reaction.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReProject implements Reaction<SlackMessagePosted> {

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SkSession session) throws IOException {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource(
                    "/com/zerocracy/radars/q-project.xml"
                )
            ),
            event.getMessageContent().trim().split("\\s+", 2)[1].trim()
        );
        final Project project = new SkProject(farm, event);
        final String tail = new Par(
            farm,
            "Remember, this chat is for managing %s project;",
            "to manage your personal profile,",
            "please open a private chat with the bot."
        ).say(project.pid());
        new ClaimOnQuestion(question, tail)
            .claim()
            .token(new SkToken(event))
            .author(new SkPerson(farm, event).uid(question.invited()))
            .param("pid", project.pid())
            .param("channel", event.getChannel().getName())
            .postTo(new ClaimsOf(farm, project));
        return question.matches();
    }

}
