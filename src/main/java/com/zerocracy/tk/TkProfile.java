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
package com.zerocracy.tk;

import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.Awards;
import com.zerocracy.pmo.People;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Properties;
import org.takes.HttpException;
import org.takes.Response;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.fork.TkRegex;
import org.takes.rs.xe.XeAppend;

/**
 * User profile page.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkProfile implements TkRegex {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param pps Properties
     * @param pkt Project
     */
    TkProfile(final Properties pps, final Project pkt) {
        this.props = pps;
        this.pmo = pkt;
    }

    @Override
    public Response act(final RqRegex req) throws IOException {
        final String login = req.matcher().group(1);
        final People people = new People(this.pmo).bootstrap();
        if (!people.find("github", login).iterator().hasNext()) {
            throw new HttpException(
                HttpURLConnection.HTTP_NOT_FOUND,
                String.format("User \"%s\" not found", login)
            );
        }
        return new RsPage(
            this.props,
            "/xsl/profile.xsl",
            req,
            new XeAppend("login", login),
            new XeAppend(
                "points",
                Integer.toString(
                    new Awards(
                        this.pmo, req.matcher().group(1)
                    ).bootstrap().total()
                )
            )
        );
    }

}
