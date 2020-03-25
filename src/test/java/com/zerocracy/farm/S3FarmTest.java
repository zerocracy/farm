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

import com.jcabi.aspects.Tv;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.TextItem;
import com.zerocracy.farm.sync.Locks;
import com.zerocracy.farm.sync.TestLocks;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Catalog;
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link S3Farm}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class S3FarmTest {

    @Test
    public void findsProject() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        final Locks locks = new TestLocks();
        final Farm farm = new S3Farm(bucket, locks);
        farm.find("@id = 'ABCDEF123'").iterator().next();
        final Project project = farm.find("@id='ABCDEF123'").iterator().next();
        final Item item = project.acq("roles.xml");
        new TextItem(item).write("hello, world");
        MatcherAssert.assertThat(
            new TextItem(item).readAll(),
            Matchers.containsString("hello")
        );
    }

    @Test
    public void returnsEmptyListOfProjects() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-to-test"
        );
        final Locks locks = new TestLocks();
        final Farm farm = new S3Farm(bucket, locks);
        MatcherAssert.assertThat(
            farm.find("links/link[@rel='github']"),
            Matchers.emptyIterable()
        );
    }

    @Test
    public void makesProjectsSafe() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket-3"
        );
        final Locks locks = new TestLocks();
        final Farm farm = new S3Farm(bucket, locks);
        final Project project = farm.find("@id='ABCR2FE03'").iterator().next();
        new Roles(project).bootstrap();
        final Roles roles = new Roles(project);
        for (int idx = 0; idx < Tv.FIVE; ++idx) {
            final String person = String.format("yegor%d", idx);
            final String role = "QA";
            roles.assign(person, role);
            MatcherAssert.assertThat(
                roles.hasRole(person, role),
                Matchers.is(true)
            );
            roles.resign(person, role);
        }
    }

    @Test
    public void deletesProject() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket-99"
        );
        final String xpath = "@id='ABCR2FDD3'";
        final Locks locks = new TestLocks();
        final Project project = new S3Farm(bucket, locks)
            .find(xpath).iterator().next();
        new Wbs(project).bootstrap().add("gh:test/test#4");
        final String prefix = new Catalog(new S3Farm(bucket, locks))
            .findByXPath(xpath)
            .iterator().next();
        new S3Farm(bucket, locks).delete(prefix);
        MatcherAssert.assertThat(
            new Wbs(
                new S3Farm(bucket, locks).find(xpath).iterator().next()
            ).bootstrap().iterate(),
            Matchers.emptyIterable()
        );
    }
}
