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

import com.zerocracy.Farm;
import com.zerocracy.SoftException;
import com.zerocracy.farm.props.Props;
import com.zerocracy.sentry.SafeSentry;
import com.zerocracy.tools.TxtUnrecoverableError;
import java.io.IOException;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.text.TextOf;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

/**
 * Safe Telegram reaction.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ReSafe implements Reaction {

    /**
     * Origin reaction.
     */
    private final Reaction origin;

    /**
     * Ctor.
     * @param reaction Origin reaction
     */
    public ReSafe(final Reaction reaction) {
        this.origin = reaction;
    }

    @Override
    public boolean react(final TmZerocrat bot, final Farm farm,
        final Update update) throws IOException {
        return new IoCheckedFunc<>(
            new FuncWithFallback<Boolean, Boolean>(
                smart -> {
                    boolean result = false;
                    try {
                        result = this.origin.react(bot, farm, update);
                    } catch (final SoftException ex) {
                        bot.post(
                            new SendMessage()
                                .enableMarkdown(true)
                                .setChatId(update.getMessage().getChatId())
                                .setText(new TextOf(ex.getMessage()).asString())
                        );
                    }
                    return result;
                },
                new FuncOf<>(
                    throwable -> {
                        bot.post(
                            new SendMessage()
                                .enableMarkdown(true)
                                .setChatId(update.getMessage().getChatId())
                                .setText(
                                    new TxtUnrecoverableError(
                                        throwable, new Props(farm),
                                        String.format(
                                            "ID: %d, From: %s, ChatId: %d",
                                            update.getUpdateId(),
                                            update.getMessage()
                                                .getFrom().getUserName(),
                                            update.getMessage().getChatId()
                                        )
                                    ).asString()
                                )
                        );
                        new SafeSentry(farm).capture(throwable);
                        throw new IOException(throwable);
                    }
                )
            )
        ).apply(true);
    }
}
