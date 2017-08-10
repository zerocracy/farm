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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;

/**
 * Telegram listening radar.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.15
 */
public final class TelegramRadar implements AutoCloseable {

    static {
        ApiContextInitializer.init();
    }

    /**
     * Project farm.
     */
    private final Farm farm;

    /**
     * Telegram reaction.
     */
    private final Reaction reaction;

    /**
     * Telegram bots.
     */
    private final List<BotSession> bots;

    /**
     * Telegram session map by chat id.
     */
    private final Map<Long, TmSession> sessions;

    /**
     * Ctor.
     * @param farm Project farm
     * @param sessions Telegram sessions
     */
    public TelegramRadar(final Farm farm, final Map<Long, TmSession> sessions) {
        this(
            farm,
            new ReNotMine(
                new ReProfile()
            ),
            sessions
        );
    }

    /**
     * Ctor.
     * @param farm Project farm
     * @param reaction Telegram reaction
     * @param sessions Telegram sessions
     */
    private TelegramRadar(
        final Farm farm,
        final Reaction reaction,
        final Map<Long, TmSession> sessions
    ) {
        this.farm = farm;
        this.reaction = reaction;
        this.sessions = sessions;
        this.bots = new LinkedList<>();
    }

    /**
     * Start listening.
     * @param token Bot token
     * @param name Bot name
     * @throws IOException If failed
     */
    public void start(
        final String token,
        final String name
    ) throws IOException {
        try {
            synchronized (this.bots) {
                this.bots.add(
                    new TelegramBotsApi().registerBot(
                        new TmZerocrat(
                            token,
                            name,
                            new BotUpdateReaction(
                                this.reaction,
                                this.farm,
                                this.sessions
                            )
                        )
                    )
                );
            }
        } catch (final TelegramApiRequestException err) {
            throw new IOException(err);
        }
    }

    @Override
    public void close() {
        synchronized (this.bots) {
            for (final BotSession session : this.bots) {
                session.stop();
            }
            this.bots.clear();
        }
    }
}
