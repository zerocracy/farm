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
package com.zerocracy.pm;

import com.zerocracy.jstk.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.misc.Concat;
import org.xembly.Directive;

/**
 * Test case for {@link ClaimOut}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ClaimOutTest {

    @Test
    public void chainsThem() throws Exception {
        final Claims claims = new Claims(new FkProject()).bootstrap();
        claims.add(
            new Concat<Directive>(
                new ClaimOut.Notify(
                    "test;token",
                    "hello, world"
                ),
                new ClaimOut().type("hello")
            )
        );
        MatcherAssert.assertThat(
            claims.iterate(),
            Matchers.iterableWithSize(2)
        );
    }

}
