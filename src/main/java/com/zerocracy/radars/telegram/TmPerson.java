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
import com.zerocracy.pmo.GoodPeople;
import com.zerocracy.pmo.People;
import java.io.IOException;
import org.telegram.telegrambots.api.objects.Update;

/**
 * Person in telegram.
 *
 * @since 1.0
 */
final class TmPerson {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Telegram update.
     */
    private final Update update;

    /**
     * Ctor.
     * @param frm Farm
     * @param upd Update
     */
    TmPerson(final Farm frm, final Update upd) {
        this.farm = frm;
        this.update = upd;
    }

    /**
     * User ID.
     * @param invited The user must be invited
     * @return User ID
     * @throws IOException If fails
     */
    public String uid(final boolean invited) throws IOException {
        return new GoodPeople(new People(this.farm)).get(
            "telegram",
            Long.toString(this.update.getMessage().getChatId()),
            invited
        );
    }
}
