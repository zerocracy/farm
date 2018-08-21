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
package com.zerocracy.tk.profile;

import com.zerocracy.Farm;
import com.zerocracy.pmo.Projects;
import com.zerocracy.tk.RqUser;
import java.io.IOException;
import org.cactoos.text.JoinedText;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsText;
import org.takes.rs.RsWithType;

/**
 * WBS items that were created by given user.
 *
 * @since 1.0
 * @todo #1159:30min Change implementation of this Take to print the Wbs data
 *  created by user from request and format it using an xsl file.
 *  To do that you will need to extend `Wbs` with a method that retrieves only
 *  those elements that were created by user (see zerocracy/datum#383).
 */
public final class TkUserWbs implements TkRegex {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param farm Farm
     */
    public TkUserWbs(final Farm farm) {
        this.farm = farm;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        return new RsWithType.Text(
            new RsText(
                new JoinedText(
                    ", ",
                    new Projects(
                        this.farm,
                        new RqUser(this.farm, req, false).value()
                    ).bootstrap().iterate()
                ).asString()
            )
        );
    }
}
