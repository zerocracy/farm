/**
 * Copyright (c) 2016 Zerocracy
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

import com.zerocracy.jstk.fake.FkFarm;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.takes.Take;
import org.takes.facets.hamcrest.HmRsStatus;
import org.takes.rq.RqFake;

/**
 * Test case for {@link TkApp}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@RunWith(Parameterized.class)
public final class PingTest {

    /**
     * The URL to ping.
     */
    private final transient String url;

    /**
     * Ctor.
     * @param addr The URL to test
     */
    public PingTest(final String addr) {
        this.url = addr;
    }

    /**
     * Params for JUnit.
     * @return Parameters
     */
    @Parameterized.Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
            new Object[][] {
                {"/robots.txt"},
                {"/css/main.css"},
                {"/xsl/index.xsl"},
                {"/xsl/layout.xsl"},
                {"/"},
            }
        );
    }

    /**
     * App can render the URL.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersAllPossibleUrls() throws Exception {
        final Take take = new TkApp(new FkFarm());
        MatcherAssert.assertThat(
            this.url,
            take.act(new RqFake("INFO", this.url)),
            Matchers.not(
                new HmRsStatus(
                    HttpURLConnection.HTTP_NOT_FOUND
                )
            )
        );
    }

}
