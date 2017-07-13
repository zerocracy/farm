/**
 * Copyright (c) 2016-2017 Zerocracy
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
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.staff.Roles;
import java.nio.file.Files;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link S3Farm}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class S3FarmTest {

    @Test
    public void findsProject() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        final Farm farm = new S3Farm(bucket);
        farm.find("@id = 'ABCDEF123'").iterator().next();
        final Project project = farm.find("@id='ABCDEF123'").iterator().next();
        final Item item = project.acq("roles.xml");
        MatcherAssert.assertThat(
            item.path().toFile().exists(),
            Matchers.is(false)
        );
        item.close();
        MatcherAssert.assertThat(
            new String(Files.readAllBytes(item.path())),
            Matchers.containsString("hello")
        );
    }

    @Test
    public void makesProjectsSafe() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket-3"
        );
        final Farm farm = new S3Farm(bucket);
        final Project project = farm.find("@id='ABCR2FE03'").iterator().next();
        new Roles(project).bootstrap();
        final Roles roles = new Roles(project);
        for (int idx = 0; idx < Tv.FIVE; ++idx) {
            final String person = String.format("yegor%d", idx);
            final String role = "ARC";
            roles.assign(person, role);
            MatcherAssert.assertThat(
                roles.hasRole(person, role),
                Matchers.is(true)
            );
        }
    }

}
