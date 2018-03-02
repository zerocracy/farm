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
package com.zerocracy.entry;

import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.radars.telegram.TmZerocrat;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;

/**
 * Telegram bot.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class ExtTelegram implements Scalar<TmZerocrat> {

    /**
     * The singleton.
     */
    private static final UncheckedFunc<Farm, TmZerocrat> SINGLETON =
        new UncheckedFunc<>(
            new SolidFunc<Farm, TmZerocrat>(
                frm -> {
                    ApiContextInitializer.init();
                    final TmZerocrat bot = new TmZerocrat(frm);
                    new TelegramBotsApi().registerBot(bot);
                    Logger.info(ExtTelegram.class, "Telegram bot registered");
                    return bot;
                }
            )
        );

    /**
     * The farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm The farm
     */
    public ExtTelegram(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public TmZerocrat value() {
        return ExtTelegram.SINGLETON.apply(this.farm);
    }

}
