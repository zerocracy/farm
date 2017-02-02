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
package com.zerocracy.stk;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.jstk.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link TmXpath}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class TmXpathTest {

    /**
     * Passes through.
     * @throws Exception If some problem inside
     */
    @Test
    public void passesThrough() throws Exception {
        MatcherAssert.assertThat(
            new TmXpath("type='test'").fits(
                new FkProject(),
                new XMLDocument(
                    "<claims><claim><type>test</type></claim></claims>"
                ).nodes("/claims/claim").get(0)
            ),
            Matchers.is(true)
        );
    }

    /**
     * Ignores if doesn't match.
     * @throws Exception If some problem inside
     */
    @Test
    public void ignoresWhatDoesntMatch() throws Exception {
        MatcherAssert.assertThat(
            new TmXpath("type='bye'").fits(
                new FkProject(),
                new XMLDocument(
                    "<claims><claim><type>hello</type></claim></claims>"
                ).nodes("/claims/claim ").get(0)
            ),
            Matchers.is(false)
        );
    }

}
