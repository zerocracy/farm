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
package com.zerocracy.pm.time;

import com.zerocracy.farm.fake.FkProject;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for reminders.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RemindersTest {

    @Test
    public void labels() throws IOException {
        final Reminders reminders = new Reminders(new FkProject()).bootstrap();
        final String job = "gh:test/test#1";
        reminders.add(job, "test", "5 days");
        MatcherAssert.assertThat(
            "Labels were not found",
            reminders.labels(job),
            Matchers.contains(
                Matchers.<String>startsWith("5")
            )
        );
    }
}
