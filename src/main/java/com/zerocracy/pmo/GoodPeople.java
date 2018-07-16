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
package com.zerocracy.pmo;

import com.zerocracy.Par;
import com.zerocracy.SoftException;
import java.io.IOException;
import java.util.Iterator;
import org.takes.misc.Href;

/**
 * People that we know.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class GoodPeople {

    /**
     * People.
     */
    private final People people;

    /**
     * Ctor.
     * @param ppl People
     */
    public GoodPeople(final People ppl) {
        this.people = ppl;
    }

    /**
     * Get user ID by alias.
     * @param rel REL
     * @param alias Alias
     * @param invited The user must be invited
     * @return User ID
     * @throws IOException If fails
     */
    public String get(final String rel, final String alias,
        final boolean invited) throws IOException {
        this.people.bootstrap();
        final Iterator<String> list = this.people.find(rel, alias).iterator();
        if (!list.hasNext()) {
            throw new SoftException(
                new Par("I don't know who you are, please click here: %s").say(
                    new Href("https://www.0crat.com/alias")
                        .with("rel", rel)
                        .with("href", alias)
                )
            );
        }
        final String uid = list.next();
        if (invited && !this.people.hasMentor(uid)) {
            throw new SoftException(
                new Par(
                    "You must be invited in order to do that, see §1"
                ).say()
            );
        }
        return uid;
    }

}
