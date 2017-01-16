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

import com.jcabi.aspects.Tv;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.mock.MkBucket;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.hr.Roles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link S3Farm}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
public final class S3FarmTest {

    /**
     * S3Farm can find a project.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsProject() throws Exception {
        final Bucket bucket = new MkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        final Farm farm = new S3Farm(bucket);
        farm.find("@id = 'ABCDEF123'").iterator().next();
        final Project project = farm.find("@id='ABCDEF123'").iterator().next();
        final Item item = project.acq("test");
        MatcherAssert.assertThat(
            item.path().toFile().exists(),
            Matchers.is(true)
        );
        Files.write(item.path(), "hello, world".getBytes());
        item.close();
        MatcherAssert.assertThat(
            new String(Files.readAllBytes(item.path())),
            Matchers.containsString("hello")
        );
    }

    /**
     * S3Farm can make projects safe.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesProjectsSafe() throws Exception {
        final Bucket bucket = new MkBucket(
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

    /**
     * S3Farm can make projects thread safe.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesProjectsThreadSafe() throws Exception {
        final Bucket bucket = new MkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        final Farm farm = new S3Farm(bucket);
        final Project project = farm.find("@id='ABCZZFE03'").iterator().next();
        new Roles(project).bootstrap();
        final int threads = Runtime.getRuntime().availableProcessors() << 2;
        final ExecutorService service = Executors.newFixedThreadPool(threads);
        final CountDownLatch latch = new CountDownLatch(1);
        final Collection<Future<Boolean>> futures = new ArrayList<>(threads);
        final String role = "PO";
        final Roles roles = new Roles(project);
        for (int thread = 0; thread < threads; ++thread) {
            final String person = String.format("jeff%d", thread);
            futures.add(
                service.submit(
                    () -> {
                        latch.await();
                        roles.assign(person, role);
                        return roles.hasRole(person, role);
                    }
                )
            );
        }
        latch.countDown();
        for (final Future<Boolean> future : futures) {
            MatcherAssert.assertThat(
                future.get(),
                Matchers.is(true)
            );
        }
    }

}
