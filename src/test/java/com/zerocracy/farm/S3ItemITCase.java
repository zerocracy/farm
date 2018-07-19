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
package com.zerocracy.farm;

import com.jcabi.s3.Ocket;
import com.jcabi.s3.Region;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Integration case for {@link S3Item}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class S3ItemITCase {

    @Test
    @Ignore
    public void modifiesFiles() throws Exception {
        final Ocket ocket = new Region.Simple(
            "AKIAIVWDGPW27GZ76BEQ",
            "-- secret --"
        ).bucket("data.0crat.com").ocket("test.xml");
        try (final Item item = new S3Item(ocket)) {
            new Xocument(item).bootstrap("pm/staff/roles");
            new Xocument(item).modify(
                new Directives().xpath("/roles")
                    .add("person")
                    .attr("id", "yegor256")
                    .add("role").set("ARC")
            );
        }
        try (final Item item = new S3Item(ocket)) {
            MatcherAssert.assertThat(
                new Xocument(item).xpath("/roles/text()"),
                Matchers.not(Matchers.emptyIterable())
            );
        }
    }

}
