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
package com.zerocracy.pm.scope;

import com.zerocracy.jstk.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Wbs}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class WbsTest {

    /**
     * Adds and removes jobs.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsJobs() throws Exception {
        final Wbs wbs = new Wbs(new FkProject()).bootstrap();
        MatcherAssert.assertThat(
            wbs.print(),
            Matchers.containsString("empty")
        );
    }

    /**
     * Adds and removes jobs.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsAndRemovesJobs() throws Exception {
        final Wbs wbs = new Wbs(new FkProject()).bootstrap();
        final String job = "gh:yegor256/0pdd#3";
        wbs.add(job);
        wbs.remove(job);
    }

}
