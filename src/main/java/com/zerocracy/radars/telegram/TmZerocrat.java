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
package com.zerocracy.radars.telegram;

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Actual bot implementation.
 *
 * @since 1.0
 */
public final class TmZerocrat extends TelegramLongPollingBot {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Bot reaction.
     */
    private final Reaction reaction;
    /**
     * Test credentials.
     */
    private final String test;

    /**
     * Ctor.
     * @param frm The farm
     * @param cred Test credentials
     */
    public TmZerocrat(final Farm frm, final String cred) {
        this(frm, cred, new ReSafe(new ReProfile()));
    }

    /**
     * Ctor.
     * @param frm The farm
     * @param cred Test credentials
     * @param rtn Bot reaction.
     */
    TmZerocrat(final Farm frm, final String cred, final Reaction rtn) {
        super();
        this.farm = frm;
        this.test = cred;
        this.reaction = rtn;
    }

    /**
     * Post a message.
     * @param message The message
     * @throws TelegramApiException If fails
     */
    public void post(final SendMessage message) throws TelegramApiException {
        this.sendApiMethod(message);
    }

    @Override
    public void onUpdateReceived(final Update update) {
        Logger.info(
            this, "%d from @%s: %s",
            update.getMessage().getChatId(),
            update.getMessage().getFrom().getUserName(),
            update.getMessage().getText()
        );
        try {
            this.reaction.react(this, this.farm, update);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getBotUsername() {
        try {
            return new Props(this.farm)
                .get("//telegram/username", this.test.split("@")[0]);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getBotToken() {
        try {
            return new Props(this.farm)
                .get("//telegram/token", this.test.split("@")[1]);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
