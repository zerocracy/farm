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
package com.zerocracy.farm;

import com.jcabi.s3.Ocket;
import com.jcabi.s3.fake.FkOcket;
import com.zerocracy.ItemXml;
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link S3Item}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class S3ItemTest {

    @Test
    public void modifiesFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket", "roles.xml"
        );
        new ItemXml(new S3Item(ocket), "pm/staff/roles").update(
            new Directives().xpath("/roles")
                .add("person")
                .attr("id", "yegor256")
                .add("role").set("ARC")
        );
        MatcherAssert.assertThat(
            new ItemXml(new S3Item(ocket)).xpath("/roles/text()"),
            Matchers.not(Matchers.emptyIterable())
        );
    }
}
