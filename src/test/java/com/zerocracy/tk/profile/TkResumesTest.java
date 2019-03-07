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
package com.zerocracy.tk.profile;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.tk.View;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link TkResumes}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkResumesTest {

    @Test
    public void rendersXml() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(new PropsFarm(), "/u/yegor256/resumes").xml()
            ),
            Matchers.allOf(
                XhtmlMatchers.hasXPaths("/page/resumes"),
                XhtmlMatchers.hasXPaths("/page/inviter"),
                XhtmlMatchers.hasXPaths("/page/filter")
            )
        );
    }

    @Test
    public void rendersResumes() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(new PropsFarm(), "/u/yegor256/resumes").html()
            ),
            XhtmlMatchers.hasXPaths("//xhtml:body")
        );
    }
}
