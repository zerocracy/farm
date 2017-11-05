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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.zerocracy.farm.MismatchException;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.jstk.farm.fake.FkProject;
import com.zerocracy.jstk.farm.fake.FkStakeholder;
import java.io.IOException;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.LengthOf;
import org.cactoos.iterable.Repeated;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link StkPool}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16.1
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class StkPoolTest {

    @Test
    public void keepSuccessfulStakeholders() throws Exception {
        final int successful = 4;
        final int failed = 1;
        final FkProject project = new FkProject();
        final StkPool pool = new StkPool(
            new Joined<Stakeholder>(
                new Repeated<>(new FkStakeholder(), successful),
                new Repeated<>(new StkPoolTest.FkErrStakeholder(), failed)
            )
        );
        final XML claim = new XMLDocument(
            "<claim><type>test</type></claim>"
        ).nodes("/claim").get(0);
        try (final StkPooled items = pool.stakeholders(project, claim)) {
            new And(
                items,
                (Stakeholder stk) -> StkPoolTest.process(stk, project, claim)
            ).value();
        }
        try (final StkPooled items = pool.stakeholders(project, claim)) {
            MatcherAssert.assertThat(
                "Didn't cache successful stakeholders",
                new LengthOf(items).value(),
                Matchers.equalTo(successful)
            );
        }
    }

    /**
     * Process stakeholder.
     * @param stakeholder Stakeholder
     * @param project Project
     * @param xml XML
     * @throws IOException If failed
     */
    @SuppressWarnings({"EmptyCatchBlock", "PMD.EmptyCatchBlock"})
    private static void process(final Stakeholder stakeholder,
        final Project project, final XML xml) throws IOException {
        try {
            stakeholder.process(project, xml);
        } catch (final MismatchException err) {
        }
    }

    /**
     * Always fail stakeholder.
     */
    private static final class FkErrStakeholder implements Stakeholder {

        @Override
        public void process(final Project project, final XML claim)
            throws IOException {
            throw new MismatchException("test");
        }
    }
}
