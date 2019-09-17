/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.gh;

import java.util.Arrays;
import java.util.List;
import org.cactoos.text.TextOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link License}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class LicenseTest {

    @Test
    public void openSourceLicensesAreValid() throws Exception {
        final List<String> lst = Arrays.asList(
            "apache-2.0",
            "bsd-2-clause", "bsd-3-clause", "bsd-3-clause-clear",
            "wtfpl",
            "gpl", "gpl-2.0", "gpl-3.0", "lgpl", "lgpl-2.1", "lgpl-3.0",
            "mit"
        );
        for (final String key : lst) {
            MatcherAssert.assertThat(
                String.format("License key %s is not valid", key),
                new License(new TextOf(key)).oss(),
                Matchers.is(true)
            );
        }
    }

    @Test
    public void closedLicenseIsNotOss() throws Exception {
        MatcherAssert.assertThat(
            new License(new TextOf("other")).oss(), Matchers.is(false)
        );
    }
}
