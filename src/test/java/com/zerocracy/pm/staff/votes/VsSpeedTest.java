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
package com.zerocracy.pm.staff.votes;

import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pmo.Speed;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.ListOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link VsSpeed}.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.19
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumber (500 line)
 */
public final class VsSpeedTest {

    @Test
    public void fastTest() throws Exception {
        final FkProject pkt = new FkProject();
        final String login = "user1";
        final Speed speed = new Speed(pkt, login).bootstrap();
        speed.add(
            "TST000001",
            "gh:test/test#1",
            TimeUnit.HOURS.toMinutes(1L)
        );
        MatcherAssert.assertThat(
            new VsSpeed(pkt, new ListOf<>("jeff", login)).take(
                login, new StringBuilder(0)
            ),
            Matchers.equalTo(0.5d)
        );
    }

    @Test
    public void slowTest() throws Exception {
        final FkProject pkt = new FkProject();
        final String login = "user2";
        final Speed speed = new Speed(pkt, login).bootstrap();
        speed.add(
            "TST000002",
            "gh:test/test#2",
            TimeUnit.DAYS.toMinutes(9L)
        );
        MatcherAssert.assertThat(
            new VsSpeed(pkt, new ListOf<>(login)).take(
                login, new StringBuilder(0)
            ),
            Matchers.equalTo(1.0)
        );
    }
}
