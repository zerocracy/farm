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

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Speed}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class SpeedTest {

    @Test
    public void addsSpeed() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Speed speed = new Speed(farm, "g4s8").bootstrap();
        speed.add("TST000001", "gh:test/test#1", 2L, Instant.now());
        speed.add("TST000002", "gh:test/test#2", 1L, Instant.now());
        MatcherAssert.assertThat(speed.jobs(), Matchers.iterableWithSize(2));
    }

    @Test
    public void avgTest() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        final Speed speed = new Speed(farm, "fast").bootstrap();
        speed.add("TST100001", "gh:test/fast#1", 1L, Instant.now());
        speed.add("TST100002", "gh:test/fast#2", 2L, Instant.now());
        // @checkstyle MagicNumber (1 line)
        speed.add("TST100003", "gh:test/fast#3", 3L, Instant.now());
        MatcherAssert.assertThat(speed.avg(), Matchers.equalTo(2.0));
    }

    @Test
    public void avgEmptyTest() throws Exception {
        final FkProject project = new FkProject();
        final FkFarm farm = new FkFarm(project);
        MatcherAssert.assertThat(
            new Speed(farm, "user")
                .bootstrap()
                .avg(),
            Matchers.equalTo(0.0)
        );
    }

}
