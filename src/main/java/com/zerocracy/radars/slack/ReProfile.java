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
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.radars.Question;
import java.io.IOException;

/**
 * Profile reaction.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class ReProfile implements Reaction<SlackMessagePosted> {

    @Override
    public boolean react(final Farm farm, final SlackMessagePosted event,
        final SlackSession session) throws IOException {
        final Question question = new Question(
            new XMLDocument(this.getClass().getResource("q-profile.xml")),
            event.getMessageContent().split("\\s+", 2)[1].trim()
        );
        if (question.matches()) {
            try (final Claims claims = new Claims(new Pmo(farm)).lock()) {
                claims.add(
                    new ClaimOut()
                        .type(question.code())
                        .token(new SkToken(event))
                        .author(new SkPerson(farm, event))
                        .params(question.params())
                );
            }
        }
        return question.matches();
    }

}
