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
package com.zerocracy.tk;

import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.PropsFarm;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.codecs.Codec;

/**
 * Test case for {@link CcSecure}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class CcSecureTest {

    @Test
    public void encodesAndDecodes() throws Exception {
        final Codec codec = new CcSecure(new PropsFarm(new FkFarm()));
        final String encoding = "UTF-8";
        final String code = new String(
            codec.encode(new Identity.Simple("urn:github:yegor256")),
            encoding
        );
        MatcherAssert.assertThat(
            codec.decode(code.getBytes(encoding)).urn(),
            Matchers.endsWith("yegor256")
        );
    }

}
