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
package com.zerocracy.pm.staff.bans;

import java.util.LinkedList;

/**
 * Fake bans.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @see Bans
 * @since 0.13
 */
public final class FkBans implements Bans {

    /**
     * Const reasons.
     */
    private final Iterable<String> rsn;

    /**
     * Ctor.
     */
    public FkBans() {
        this(new LinkedList<>());
    }

    /**
     * Ctor.
     * @param rsn Reasons
     */
    public FkBans(final Iterable<String> rsn) {
        this.rsn = rsn;
    }

    @Override
    public Iterable<String> reasons(final String job, final String user) {
        return this.rsn;
    }
}
