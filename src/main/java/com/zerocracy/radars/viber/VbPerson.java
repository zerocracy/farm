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
package com.zerocracy.radars.viber;

import com.zerocracy.Farm;
import com.zerocracy.pmo.GoodPeople;
import com.zerocracy.pmo.People;
import java.io.IOException;

/**
 * Person in Viber.
 *
 * @since 1.0
 */
final class VbPerson {
    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Viber message.
     */
    private final VbEvent.Message msg;

    /**
     * Ctor.
     * @param frm Farm
     * @param msg Viber message.
     */
    VbPerson(final Farm frm, final VbEvent.Message msg) {
        this.farm = frm;
        this.msg = msg;
    }

    /**
     * User ID.
     * @param invited The user must be invited
     * @return User ID
     * @throws IOException If fails
     */
    public String uid(final boolean invited) throws IOException {
        return new GoodPeople(new People(this.farm))
            .get("viber", this.msg.vid(), invited);
    }
}
