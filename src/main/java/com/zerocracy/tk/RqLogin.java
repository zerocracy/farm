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
import com.zerocracy.pmo.People;
import java.io.IOException;
import org.cactoos.Scalar;
import org.takes.facets.fork.RqRegex;
import org.takes.facets.forward.RsFailure;

/**
 * User login from the request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RqLogin implements Scalar<String> {

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * RqRegex.
     */
    private final RqRegex request;

    /**
     * Ctor.
     * @param pkt Project
     * @param req Request
     */
    public RqLogin(final Project pkt, final RqRegex req) {
        this.pmo = pkt;
        this.request = req;
    }

    @Override
    public String value() throws IOException {
        final String login = this.request.matcher().group(1);
        final People people = new People(this.pmo).bootstrap();
        if (!people.find("github", login).iterator().hasNext()) {
            throw new RsFailure(
                String.format("User \"@%s\" not found", login)
            );
        }
        return login;
    }
}
