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

import java.io.IOException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Telegram bot session.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.15
 */
final class TmBotSession implements TmSession {

    /**
     * Telegram bot.
     */
    private final DefaultAbsSender bot;

    /**
     * Chat id.
     */
    private final long chat;

    /**
     * Ctor.
     * @param bot A bot
     * @param chat Chat id
     */
    TmBotSession(final DefaultAbsSender bot, final long chat) {
        this.bot = bot;
        this.chat = chat;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void reply(final TmResponse response) throws IOException {
        try {
            this.bot.sendMessage(
                new SendMessage()
                    .setChatId(this.chat)
                    .setText(response.text())
            );
        } catch (final TelegramApiException err) {
            throw new IOException("Telegram API error", err);
        }
    }

    @Override
    public String bot() throws IOException {
        try {
            return this.bot.getMe().getUserName();
        } catch (final TelegramApiException err) {
            throw new IOException(err);
        }
    }
}
