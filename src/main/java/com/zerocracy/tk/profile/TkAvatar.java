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
package com.zerocracy.tk.profile;

import com.jcabi.github.Github;
import com.jcabi.github.User;
import java.io.IOException;
import java.util.Locale;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.RsRedirect;

/**
 * User avatar image.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkAvatar implements TkRegex {

    /**
     * GitHub.
     */
    private final Github github;

    /**
     * Ctor.
     * @param hub The github
     */
    public TkAvatar(final Github hub) {
        this.github = hub;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        return new RsRedirect(
            new User.Smart(
                this.github.users().get(
                    req.matcher().group(1).toLowerCase(Locale.ENGLISH)
                )
            ).avatarUrl().toString()
        );
    }

}
