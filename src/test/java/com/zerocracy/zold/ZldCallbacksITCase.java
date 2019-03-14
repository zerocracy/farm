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
package com.zerocracy.zold;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.tools.RandomString;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link ZldCallbacks}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class ZldCallbacksITCase {

    @BeforeClass
    public static void check() {
        Assume.assumeNotNull(System.getProperty("pgsql.port"));
    }

    @Test
    public void addCallbackAndReadTheProject() throws Exception {
        final Farm farm = new PropsFarm();
        final ZldCallbacks cbs =
            new ZldCallbacks(new ExtDataSource(farm).value(), farm);
        final String pid = new FkProject().pid();
        final String code = new RandomString(Tv.EIGHT).asString();
        final String secret = new RandomString(Tv.EIGHT).asString();
        final String cid = new RandomString(Tv.EIGHT).asString();
        final String prefix = "ertyu7iu6y5t@afsdfgs4r3";
        cbs.add(pid, cid, code, secret, prefix);
        MatcherAssert.assertThat(
            cbs.take(cid, code, secret, prefix).pid(),
            Matchers.equalTo(pid)
        );
    }
}
