/**
 * Copyright (c) 2016 Zerocracy
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

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.hr.Roles;
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link S3Project}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class S3ProjectTest {

    /**
     * S3Project can modify.
     * @throws Exception If some problem inside
     */
    @Test
    public void modifiesItems() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        final Project project = new S3Project(bucket, "");
        new Roles(project).bootstrap();
        final String person = "yegor256";
        final String role = "PO";
        new Roles(project).assign(person, role);
        MatcherAssert.assertThat(
            new Roles(project).hasRole(person, role),
            Matchers.is(true)
        );
    }

    /**
     * S3Project can modify from two entry points.
     * @throws Exception If some problem inside
     */
    @Test
    public void modifiesItemsTwoEntryPoints() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "my-bucket"
        );
        final Project project = new S3Project(bucket, "");
        final String file = "roles.xml";
        final String login = "davvd";
        final String role = "ARC";
        try (final Item item = project.acq(file)) {
            try (final Item sub = project.acq(file)) {
                new Xocument(sub).bootstrap("pm/hr/roles");
            }
            new Xocument(item).modify(
                new Directives().xpath("/roles")
                    .add("person")
                    .attr("id", login)
                    .add("role").set(role)
            );
        }
        MatcherAssert.assertThat(
            new Roles(project).hasRole(login, role),
            Matchers.is(true)
        );
    }

}
