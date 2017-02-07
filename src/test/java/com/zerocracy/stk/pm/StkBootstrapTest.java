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
package com.zerocracy.stk.pm;

import com.jcabi.xml.XMLDocument;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.jstk.fake.FkProject;
import org.junit.Test;

/**
 * Test case for {@link StkBootstrap}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class StkBootstrapTest {

    /**
     * Bootstraps a project.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void bootstrapsProjects() throws Exception {
        final Stakeholder stk = new StkBootstrap();
        for (int idx = 0; idx < 2; ++idx) {
            stk.process(
                new FkProject(),
                new XMLDocument(
                    "<claim><token>X</token><author>yegor</author></claim>"
                ).nodes("/claim").get(0)
            );
        }
    }

}
