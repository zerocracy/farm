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
package com.zerocracy;

import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Par}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class ParTest {

    @Test
    public void replacesKeyElements() throws Exception {
        MatcherAssert.assertThat(
            new Par(
                "@%s is a DEV",
                "in C63314D6Z",
                "and has %d job(s), as in §1",
                "see [me](/p/PMO?a=1)"
            ).print("yegor256", 1),
            Matchers.equalTo(
                new JoinedText(
                    " ",
                    "[@yegor256](http://www.0crat.com/u/yegor256) is a `DEV`",
                    "in [`C63314D6Z`](http://www.0crat.com/p/C63314D6Z)",
                    "and has one job, as in",
                    "[§1](http://datum.zerocracy.com/pages/policy.html#1)",
                    "see [me](http://www.0crat.com/p/PMO?a=1)"
                ).asString()
            )
        );
    }

}
