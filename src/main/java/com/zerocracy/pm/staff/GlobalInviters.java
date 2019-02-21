/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.pm.staff;

import com.zerocracy.Farm;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import org.cactoos.list.ListOf;

/**
 * List of users who can invite any person.
 * See https://github.com/zerocracy/farm/issues/1410
 *
 * @since 1.0
 * @todo #1775:30min Reimplement this class - it should return
 *  not constant set of users, but all users who has any role
 *  in PMO project and users who has QA role in Zerocracy project.
 */
public final class GlobalInviters extends AbstractSet<String> {

    /**
     * Users.
     */
    private static final List<String> USERS = new ListOf<>(
        "yegor256",
        "ypshenychka",
        "g4s8"
    );

    /**
     * Ctor.
     * @param farm Farm
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public GlobalInviters(final Farm farm) {
        super();
    }

    @Override
    public Iterator<String> iterator() {
        return GlobalInviters.USERS.iterator();
    }

    @Override
    public int size() {
        return GlobalInviters.USERS.size();
    }
}
