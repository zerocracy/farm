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
package com.zerocracy.pmo.recharge;

import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link Stripe}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class StripeTest {

    @BeforeClass
    public static void setupClass() throws Exception {
        Assume.assumeTrue(
            "stripe secret is not configured",
            new Props().has("//stripe/secret")
        );
    }

    @Test
    public void calculatesDailyRevenue() throws Exception {
        MatcherAssert.assertThat(
            new Stripe(new PropsFarm()).dailyRevenue("USD")
                .compareTo(BigDecimal.ZERO),
            Matchers.equalTo(1)
        );
    }
}
