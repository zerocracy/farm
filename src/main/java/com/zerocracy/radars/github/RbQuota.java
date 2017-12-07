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
package com.zerocracy.radars.github;

import com.jcabi.github.Github;
import com.jcabi.github.Limit;
import com.jcabi.github.Limits;
import com.zerocracy.jstk.Farm;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.json.JsonObject;
import org.takes.facets.forward.RsForward;
import org.takes.rs.RsWithBody;

/**
 * Rebound that acts only if GitHub has API quota available.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 */
public final class RbQuota implements Rebound {

    /**
     * Original reaction.
     */
    private final Rebound origin;

    /**
     * Ctor.
     * @param rtn Reaction
     */
    public RbQuota(final Rebound rtn) {
        this.origin = rtn;
    }

    @Override
    public String react(final Farm farm, final Github github,
        final JsonObject event) throws IOException {
        final Limit.Smart limit = new Limit.Smart(
            github.limits().get(Limits.CORE)
        );
        // @checkstyle MagicNumber (1 line)
        if (limit.remaining() < 500) {
            throw new RsForward(
                new RsWithBody(
                    String.format(
                        "GitHub over quota: limit=%d, remaining=%d, reset=%s",
                        limit.limit(), limit.remaining(), limit.reset()
                    )
                ),
                HttpURLConnection.HTTP_UNAVAILABLE
            );
        }
        return this.origin.react(farm, github, event);
    }

}
