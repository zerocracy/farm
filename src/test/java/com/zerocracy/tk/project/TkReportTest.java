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
package com.zerocracy.tk.project;

import com.jcabi.matchers.XhtmlMatchers;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.footprint.FtFarm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.tk.View;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link TkReport}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkReportTest {

    @Test
    public void rendersReport() throws Exception {
        final Farm farm = new FtFarm(new PropsFarm(new FkFarm()));
        final String uid = "yegor256";
        final Project project = farm.find("@id='C00000000'").iterator().next();
        new ClaimOut()
            .type("Order was given")
            .param("login", uid)
            .postTo(new ClaimsOf(farm, project));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new View(
                    farm,
                    "/report/C00000000?report=orders-given-by-week"
                ).xml()
            ),
            XhtmlMatchers.hasXPaths("/page/rows/row[week]")
        );
    }

}
