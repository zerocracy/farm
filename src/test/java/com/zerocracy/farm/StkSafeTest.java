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
package com.zerocracy.farm;

import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import org.cactoos.iterable.LengthOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Test case for {@link StkSafe}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class StkSafeTest {

    @Test
    public void catchesSoftException() throws Exception {
        final Stakeholder stk = Mockito.mock(Stakeholder.class);
        final Project project = new FkProject();
        new ClaimOut().type("hello you")
            .postTo(new ClaimsOf(new PropsFarm(), project));
        final XML claim = new ClaimsItem(project).iterate().iterator().next();
        Mockito.doThrow(new SoftException("")).when(stk).process(
            project, claim
        );
        new StkSafe("hello", new PropsFarm(), stk).process(project, claim);
    }

    @Test
    public void dontRepostNotifyFailures() throws Exception {
        final Project project = new FkProject();
        new ClaimOut()
            .type("Notify GitHub")
            .token("github;test/test#1")
            .postTo(new ClaimsOf(new PropsFarm(), project));
        final XML claim = new ClaimsItem(project).iterate().iterator().next();
        new StkSafe(
            "hello1",
            new StkSafeTest.NonTestingFarm(),
            new StkSafeTest.StkError()
        ).process(project, claim);
        MatcherAssert.assertThat(
            new ClaimsItem(project).iterate(),
            Matchers.iterableWithSize(2)
        );
    }

    @Test
    public void dontRepeatErrorClaims() throws Exception {
        final FkProject project = new FkProject();
        new ClaimOut().type("Error").postTo(
            new ClaimsOf(new PropsFarm(), project)
        );
        final int before = new LengthOf(new ClaimsItem(project).iterate())
            .intValue();
        new StkSafe(
            "errors1",
            new StkSafeTest.NonTestingFarm(),
            new StkSafeTest.StkError()
        ).process(
            project,
            new ClaimsItem(project).iterate().iterator().next()
        );
        MatcherAssert.assertThat(
            new ClaimsItem(project).iterate(),
            Matchers.iterableWithSize(before)
        );
    }

    /**
     * Always failing stakeholder.
     */
    private static final class StkError implements Stakeholder {
        @Override
        public void process(final Project project, final XML claim) {
            throw new IllegalStateException("error");
        }
    }

    /**
     * Props farm without testing flag.
     */
    private static class NonTestingFarm implements Farm {

        /**
         * Farm.
         */
        private final Farm frm;

        /**
         * Ctor.
         */
        NonTestingFarm() {
            this.frm = new PropsFarm(
                new Directives().xpath("/props/testing").remove()
            );
        }

        @Override
        public Iterable<Project> find(final String xpath) throws IOException {
            return this.frm.find(xpath);
        }
    }
}
