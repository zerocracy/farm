/*
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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.farm.MismatchException;
import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link StkSmart}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class StkSmartTest {

    @Test
    public void doesntDuplicateMistakes() throws Exception {
        final AtomicInteger hits = new AtomicInteger();
        final Stakeholder stk = new StkSmart(
            (project, xml) -> {
                hits.incrementAndGet();
                throw new MismatchException("oops");
            }
        );
        final XML claim = new XMLDocument(
            "<claim><type>test</type></claim>"
        ).nodes("/claim").get(0);
        final Project project = new FkProject();
        for (int idx = 0; idx < 2; ++idx) {
            stk.process(project, claim);
        }
        MatcherAssert.assertThat(hits.get(), Matchers.equalTo(1));
    }

    @Test
    public void passesNormalCallsThrough() throws Exception {
        final AtomicInteger hits = new AtomicInteger();
        final Stakeholder stk = new StkSmart(
            (project, xml) -> {
                hits.incrementAndGet();
            }
        );
        final XML claim = new XMLDocument(
            "<claim><type>test me</type></claim>"
        ).nodes("/claim ").get(0);
        final Project project = new FkProject();
        for (int idx = 0; idx < 2; ++idx) {
            stk.process(project, claim);
        }
        MatcherAssert.assertThat(hits.get(), Matchers.equalTo(2));
    }

    @Test
    public void passesBrokenCallsThrough() throws Exception {
        final AtomicInteger hits = new AtomicInteger();
        final Stakeholder stk = new StkSmart(
            (project, xml) -> {
                hits.incrementAndGet();
                throw new IOException("oops here");
            }
        );
        final XML claim = new XMLDocument(
            "<claim><type>test me now</type></claim>"
        ).nodes("/claim  ").get(0);
        final Project project = new FkProject();
        for (int idx = 0; idx < 2; ++idx) {
            try {
                stk.process(project, claim);
            } catch (final IOException ex) {
                assert ex != null;
            }
        }
        MatcherAssert.assertThat(hits.get(), Matchers.equalTo(2));
    }

}
