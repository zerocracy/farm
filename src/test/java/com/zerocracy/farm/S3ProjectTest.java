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

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Agenda;
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link S3Project}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class S3ProjectTest {

    @Test
    public void modifiesItems() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        final Project project = new S3Project(bucket, "A1B2C3D4F");
        final Farm farm = new S3Farm(bucket);
        final String person = "yegor256";
        new Agenda(farm, person).bootstrap();
        final String job = "gh:test/test#1";
        new Agenda(farm, person).add(project, job, "QA");
        MatcherAssert.assertThat(
            new Agenda(farm, person).jobs(),
            Matchers.hasItem(job)
        );
    }

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
                new Xocument(sub).bootstrap("pm/staff/roles");
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
