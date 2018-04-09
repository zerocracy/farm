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
package com.zerocracy.pmo;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.farm.fake.FkFarm;
import org.cactoos.io.LengthOf;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link Options}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.21
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class OptionsTest {
    /**
     * User login.
     */
    private static final String LOGIN = "user1324234";
    /**
     * Farm with options.
     */
    private Farm farm;

    @Before
    public void setUp() throws Exception {
        this.farm = new FkFarm();
        try (
            final Item item = new Pmo(this.farm)
                .acq(String.format("options/%s.xml", OptionsTest.LOGIN))
        ) {
            new LengthOf(
                new TeeInput(
                    new ResourceOf("com/zerocracy/pmo/options.xml"),
                    item.path()
                )
            ).intValue();
        }
    }

    @Test
    public void readMaxJobs() throws Exception {
        MatcherAssert.assertThat(
            new Options(new Pmo(this.farm), OptionsTest.LOGIN)
                .bootstrap()
                .maxJobsInAgenda(0),
            Matchers.equalTo(2)
        );
    }

    @Test
    public void readNotifyStudents() throws Exception {
        MatcherAssert.assertThat(
            new Options(new Pmo(this.farm), OptionsTest.LOGIN)
                .bootstrap()
                .notifyStudents(false),
            Matchers.is(true)
        );
    }

    @Test
    public void readNotifyRfps() throws Exception {
        MatcherAssert.assertThat(
            new Options(new Pmo(this.farm), OptionsTest.LOGIN)
                .bootstrap()
                .notifyRfps(false),
            Matchers.is(true)
        );
    }

    @Test
    public void notifyPublish() throws Exception {
        MatcherAssert.assertThat(
            new Options(new Pmo(this.farm), OptionsTest.LOGIN)
                .bootstrap()
                .notifyPublish(false),
            Matchers.is(true)
        );
    }
}
