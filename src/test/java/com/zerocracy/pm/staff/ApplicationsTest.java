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
package com.zerocracy.pm.staff;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.fake.FkProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Applications}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ApplicationsTest {

    @Test
    public void submitApllication() throws Exception {
        final Applications apps = new Applications(new FkProject())
            .bootstrap();
        final String login = "g4s8";
        final String role = "DEV";
        final Cash rate = new Cash.S("$100");
        apps.submit(login, role, rate);
        MatcherAssert.assertThat(
            apps.all(),
            Matchers.contains(
                XhtmlMatchers.hasXPaths(
                    String.format("/application[@login = '%s']", login),
                    String.format("/application/role[text() = '%s']", role),
                    String.format(
                        "/application/rate[text() = '%s']", rate.toString()
                    )
                )
            )
        );
    }
}
