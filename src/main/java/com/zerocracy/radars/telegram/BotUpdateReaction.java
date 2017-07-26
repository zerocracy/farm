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

import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.util.Map;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.DefaultAbsSender;

/**
 * Reaction to bot update.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.15
 */
final class BotUpdateReaction {

    /**
     * Reaction.
     */
    private final Reaction reaction;

    /**
     * Project farm.
     */
    private final Farm farm;

    /**
     * Telegram sessions.
     */
    private final Map<Long, TmSession> sessions;

    /**
     * Ctor.
     * @param reaction Reaction
     * @param farm Project farm
     * @param sessions Telegram sessions
     */
    BotUpdateReaction(
        final Reaction reaction,
        final Farm farm,
        final Map<Long, TmSession> sessions
    ) {
        this.reaction = reaction;
        this.farm = farm;
        this.sessions = sessions;
    }

    /**
     * React to bot update.
     * @param update An update
     * @param bot A bot
     * @throws IOException If failed
     */
    public void react(
        final Update update,
        final DefaultAbsSender bot
    ) throws IOException {
        this.reaction.react(
            this.farm,
            this.session(update.getMessage().getChatId(), bot),
            new RqUpdate(update)
        );
    }

    /**
     * Session associated with this chat
     * @param chat Chat id
     * @param bot A bot
     * @return Session
     */
    private TmSession session(final long chat, final DefaultAbsSender bot) {
        this.sessions.putIfAbsent(chat, new TmBotSession(bot, chat));
        return this.sessions.get(chat);
    }
}
