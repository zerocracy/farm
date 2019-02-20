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
package com.zerocracy.pmo.banks;

import com.zerocracy.Farm;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link Zold}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ZoldITCase {

    @BeforeClass
    public static void setUp() throws Exception {
        Assume.assumeTrue(
            "Zold integration test is not configured",
            new Props().has("//zold/secret")
        );
    }

    @Test
    public void payZold() throws Exception {
        final Farm farm = new PropsFarm();
        final String target = "yegor256";
        new Roles(new Pmo(farm)).bootstrap().assign(target, "PO");
        MatcherAssert.assertThat(
            new Zold(farm).pay(
                target,
                new Cash.S("$0.01"),
                "ZoldITCase#payZold",
                "none"
            ),
            Matchers.not(Matchers.isEmptyString())
        );
    }

    @Test(expected = IOException.class)
    public void failPaymentForNonPmoUsers() throws Exception {
        new Zold(new PropsFarm()).pay(
            "g4s8",
            new Cash.S("$0.02"),
            "ZoldITCase#failPaymentForNonPmoUsers",
            "none2"
        );
    }
}
