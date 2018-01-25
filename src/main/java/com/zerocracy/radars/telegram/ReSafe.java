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
package com.zerocracy.radars.telegram;

import com.zerocracy.Farm;
import com.zerocracy.err.FbReaction;
import com.zerocracy.err.FbSend;
import java.io.IOException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

/**
 * Safe Telegram reaction.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReSafe implements Reaction {
    /**
     * Origin reaction.
     */
    private final Reaction origin;
    /**
     * Reaction with fallback.
     */
    private final FbReaction fbr;

    /**
     * Ctor.
     * @param reaction Origin reaction
     */
    public ReSafe(final Reaction reaction) {
        this.origin = reaction;
        this.fbr = new FbReaction();
    }

    @Override
    public boolean react(final TmZerocrat bot, final Farm farm,
        final Update update) throws IOException {
        return this.fbr.react(
            () -> this.origin.react(bot, farm, update),
            new FbSend(
                msg -> bot.post(
                    new SendMessage()
                        .enableMarkdown(true)
                        .setChatId(update.getMessage().getChatId())
                        .setText(msg)
                ),
                farm
            )
        );
    }
}
