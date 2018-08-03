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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimXml;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.reactive.Brigade;
import java.io.IOException;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link BrigadeProc}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class BrigadeProcTest {
    @Test
    public void processMessage() throws Exception {
        final Project proj = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(proj));
        new BrigadeProc(
            new Brigade(
                new AssertClaim(
                    Matchers.equalTo(proj.pid()),
                    XhtmlMatchers.hasXPaths("//type[text() = 'test']")
                )
            ),
            farm
        ).exec(
            new Message()
                .withMessageAttributes(
                    new MapOf<String, MessageAttributeValue>(
                        new MapEntry<>(
                            "project",
                            new MessageAttributeValue()
                                .withDataType("String")
                                .withStringValue(proj.pid())
                        )
                    )
                )
                .withBody(
                    new ClaimXml(
                        new ClaimOut()
                            .type("test")
                    ).asXml().toString()
                )
        );
    }

    /**
     * Stakeholder to assert claim's xml.
     */
    private static final class AssertClaim implements Stakeholder {

        /**
         * Project id matcher.
         */
        private final Matcher<String> pid;

        /**
         * Claim matcher.
         */
        private final Matcher<XML> claim;

        /**
         * Ctor.
         *
         * @param pid Project id matcher
         * @param claim Claim matcher
         */
        AssertClaim(final Matcher<String> pid,
            final Matcher<XML> claim) {
            this.pid = pid;
            this.claim = claim;
        }
        @Override
        public void process(final Project project, final XML xml)
            throws IOException {
            MatcherAssert.assertThat(
                "Project id is not correct",
                project.pid(),
                this.pid
            );
            MatcherAssert.assertThat(
                "Claim is not correct",
                xml,
                this.claim
            );
        }
    }
}
