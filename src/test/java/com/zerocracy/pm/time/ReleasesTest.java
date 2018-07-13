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
package com.zerocracy.pm.time;

import com.jcabi.aspects.Tv;
import com.zerocracy.farm.fake.FkProject;
import java.time.Instant;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Releases}.
 *
 * @since 0.25
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ReleasesTest {
    @Test
    public void findLatest() throws Exception {
        final Releases releases = new Releases(new FkProject()).bootstrap();
        // @checkstyle LineLengthCheck (4 lines)
        releases.add("test/one", "0.1", Instant.ofEpochMilli((long) Tv.THOUSAND));
        releases.add("test/two", "1.1", Instant.ofEpochMilli((long) Tv.FIVE));
        releases.add("test/three", "2.3", Instant.ofEpochMilli((long) Tv.MILLION));
        releases.add("test/four", "1.5", Instant.ofEpochMilli((long) Tv.THREE));
        MatcherAssert.assertThat(
            releases.latest(),
            Matchers.equalTo(Instant.ofEpochMilli((long) Tv.MILLION))
        );
    }

    @Test
    public void findLatestInEmptyReleases() throws Exception {
        MatcherAssert.assertThat(
            new Releases(new FkProject()).bootstrap().latest(),
            Matchers.equalTo(Instant.ofEpochMilli(0L))
        );
    }
}
