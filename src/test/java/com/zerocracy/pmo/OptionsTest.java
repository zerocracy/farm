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
import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import java.io.IOException;
import java.util.Iterator;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Test case for {@link Options}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.21
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class OptionsTest {
    @Test
    public void readMaxJobs() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(
                new Directives()
                    .addIf("options")
                    .addIf("maxJobsInAgenda")
                    .set("2")
            ).maxJobsInAgenda(0),
            Matchers.equalTo(2)
        );
    }

    @Test
    public void readNotifyStudents() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(
                new Directives().append(
                    new OptionsTest.XeNotify("students", true)
                )
            ).notifyStudents(false),
            Matchers.is(true)
        );
    }

    @Test
    public void readNotifyRfps() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(
                new Directives().append(
                    new OptionsTest.XeNotify("rfps", true)
                )
            ).notifyRfps(false),
            Matchers.is(true)
        );
    }

    @Test
    public void notifyPublish() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(
                new Directives().append(
                    new OptionsTest.XeNotify("publish", true)
                )
            ).notifyPublish(false),
            Matchers.is(true)
        );
    }

    @Test
    public void readMaxJobsDefault() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(new Directives()).maxJobsInAgenda(1),
            Matchers.equalTo(1)
        );
    }

    @Test
    public void readNotifyStudentsDefault() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(new Directives()).notifyStudents(true),
            Matchers.is(true)
        );
    }

    @Test
    public void readNotifyRfpsDefault() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(new Directives()).notifyRfps(true),
            Matchers.is(true)
        );
    }

    @Test
    public void notifyPublishDefault() throws Exception {
        MatcherAssert.assertThat(
            OptionsTest.options(new Directives()).notifyPublish(true),
            Matchers.is(true)
        );
    }

    /**
     * Make XML options.
     * @param dirs Directives
     * @return Options
     * @throws IOException If fails
     */
    private static Options options(final Iterable<Directive> dirs)
        throws IOException {
        final Farm farm = new FkFarm();
        try (
            final Item item = new Pmo(farm).acq("options/test.xml")
        ) {
            new Xocument(item).bootstrap("pmo/options").modify(dirs);
        }
        return new Options(new Pmo(farm), "test").bootstrap();
    }

    /**
     * Notify directives.
     */
    private static final class XeNotify implements Iterable<Directive> {
        /**
         * Options name.
         */
        private final String name;
        /**
         * Option value.
         */
        private final boolean val;
        /**
         * Ctor.
         * @param option Option name
         * @param value Option value
         */
        XeNotify(final String option, final boolean value) {
            this.name = option;
            this.val = value;
        }
        @Override
        public Iterator<Directive> iterator() {
            return new Directives()
                .addIf("options")
                .addIf("notify")
                .addIf(this.name)
                .set(this.val)
                .iterator();
        }
    }
}
