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
package com.zerocracy.pm.cost;

import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Boosts}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class BoostsTest {

    @Test
    public void defaultBoost() throws IOException {
        MatcherAssert.assertThat(
            "default boost factor is not 2.0",
            new Boosts(new PropsFarm(), new FkProject()).bootstrap()
                .factor("gh:test/test#1"),
            Matchers.equalTo(2)
        );
    }

    @Test
    public void addBoost() throws IOException {
        final Boosts boosts =
            new Boosts(new PropsFarm(), new FkProject()).bootstrap();
        final String job = "gh:test/test#2";
        final int factor = 4;
        boosts.boost(job, factor);
        MatcherAssert.assertThat(
            "boost has not been added",
            boosts.factor(job),
            Matchers.equalTo(factor)
        );
    }

    @Test
    public void updateBoost() throws IOException {
        final Boosts boosts =
            new Boosts(new PropsFarm(), new FkProject()).bootstrap();
        final String job = "gh:test/test#3";
        boosts.boost(job, 1);
        final int factor = 3;
        boosts.boost(job, factor);
        MatcherAssert.assertThat(
            "boost has not been updated",
            boosts.factor(job),
            Matchers.equalTo(factor)
        );
    }
}
