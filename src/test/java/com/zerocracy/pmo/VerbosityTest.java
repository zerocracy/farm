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

import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Verbosity}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
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
}
