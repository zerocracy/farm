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

import com.jcabi.log.Logger;
import java.io.IOException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Actual bot implementation.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.15
 */
final class TmZerocrat extends TelegramLongPollingBot {

    /**
     * Telegram bot token.
     */
    private final String token;

    /**
     * Telegram bot username.
     */
    private final String name;

    /**
     * Bot reaction.
     */
    private final BotUpdateReaction reaction;

    /**
     * Ctor.
     * @param token Telegram bot token
     * @param name Telegram bot username
     * @param reaction Bot reaction.
     */
    TmZerocrat(
        final String token,
        final String name,
        final BotUpdateReaction reaction
    ) {
        super();
        this.token = token;
        this.name = name;
        this.reaction = reaction;
    }

    @Override
    public void onUpdateReceived(final Update update) {
        try {
            this.react(update);
        } catch (final TelegramApiException err) {
            Logger.error(
                this,
                "Telegram API error: %[exception]s",
                err
            );
        }
    }

    @Override
    public String getBotUsername() {
        return this.name;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    /**
     * React to update.
     * @param update An update
     * @throws TelegramApiException If API error
     */
    private void react(final Update update) throws TelegramApiException {
        try {
            this.reaction.react(update, this);
        } catch (final IOException err) {
            this.sendApiMethod(
                new SendMessage(
                    update.getMessage().getChatId(),
                    err.getMessage()
                )
            );
        }
    }
}
