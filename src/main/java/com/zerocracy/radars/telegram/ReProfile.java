/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.radars.telegram;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.pmo.Pmo;
import com.zerocracy.radars.ClaimOnQuestion;
import com.zerocracy.radars.Question;
import java.io.IOException;

/**
 * Telegram profile reaction.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 */
public final class ReProfile implements Reaction {
    @Override
    public boolean react(final Farm farm, final TmSession session,
        final TmRequest request) throws IOException {
        final Question question = new Question(
            new XMLDocument(
                this.getClass().getResource(
                    "/com/zerocracy/radars/q-profile.xml"
                )
            ),
            request.text().trim()
        );
        // @checkstyle LineLength (1 line)
        new ClaimOnQuestion(question, "Remember, this chat is for managing your personal profile; to manage a project, please open or create a new channel and invite the bot there.")
            .claim()
            .token(new TmToken(request))
            .author(new TmPerson(farm, request).uid())
            .postTo(new Pmo(farm));
        return question.matches();
    }
}
