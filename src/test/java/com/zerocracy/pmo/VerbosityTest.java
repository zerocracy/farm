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
package com.zerocracy.pmo;

import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import java.util.List;
import org.cactoos.text.FormattedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsEqual;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link Verbosity}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class VerbosityTest {
    @Test
    public void addsVerbosity() throws Exception {
        final Pmo pmo = new Pmo(new FkFarm());
        final FkProject pkt = new FkProject();
        final String login = "user1234";
        final String job = "gh:test/test#1";
        new Verbosity(pmo, login).bootstrap().add(
            job,
            pkt,
            1
        );
        MatcherAssert.assertThat(
            new Xocument(
                pmo.acq("verbosity/user1234.xml")
            ).xpath(
                String.format(
                    // @checkstyle LineLengthCheck (1 line)
                    "/verbosity/order[@job = 'gh:test/test#1' and ./project/text() = '%s']/messages/text()",
                    pkt.pid()
                )
            ),
            Matchers.contains("1")
        );
    }

    @Test
    @Ignore
    public void overridesVerbosity() throws IOException {
        final Pmo pmo = new Pmo(new FkFarm());
        final FkProject pkt = new FkProject();
        final String login = "paulodamaso";
        final String job = "gh:test/test#256";
        final int newvalue = 5;
        final Verbosity verbosity = new Verbosity(pmo, login).bootstrap();
        verbosity.add(job, pkt, 1);
        verbosity.add(job, pkt, newvalue);
        try (
            Item item = pmo.acq(
                new FormattedText(
                    "verbosity/%s.xml",
                    login
                ).asString()
            )
        ) {
            final List<String> result = new Xocument(item).xpath(
                new FormattedText(
                    // @checkstyle LineLengthCheck (1 line)
                    "/verbosity/order[@job = '%s' and ./project/text() = '%s']/messages/text()",
                    job,
                    pkt.pid()
                ).asString()
            );
            MatcherAssert.assertThat(
                "Added verbosity twice",
                result,
                new IsCollectionContaining<>(
                    new IsEqual<>(
                        Integer.toString(newvalue)
                    )
                )
            );
        }
    }
}
