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

import com.zerocracy.farm.fake.FkProject;
import java.time.LocalDateTime;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Awards}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.12
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class AwardsTest {

    @Test
    public void addsAndRemovesPoints() throws Exception {
        final Awards awards = new Awards(new FkProject(), "yegor").bootstrap();
        awards.add(1, "gh:test/test#1", "just for fun 1", LocalDateTime.now());
        awards.add(-1, "gh:test/test#2", "just for fun 2", LocalDateTime.now());
        awards.add(1, "gh:test/test#3", "just for fun 3", LocalDateTime.now());
        MatcherAssert.assertThat(awards.total(), Matchers.equalTo(1));
    }

    @Test
    public void pointsOnlyLastNinetyDays() throws Exception {
        final Awards awards = new Awards(new FkProject(), "fabriciofx")
            .bootstrap();
        awards.add(
            1, "gh:test/test#1", "just for fun 1",
            LocalDateTime.now().minusDays(91)
        );
        awards.add(
            2, "gh:test/test#2", "just for fun 2",
            LocalDateTime.now().minusDays(90)
        );
        awards.add(
            3, "gh:test/test#3", "just for fun 3",
            LocalDateTime.now().minusDays(30)
        );
        awards.add(4, "gh:test/test#4", "just for fun 4", LocalDateTime.now());
        MatcherAssert.assertThat(awards.total(), Matchers.equalTo(9));
    }

}
