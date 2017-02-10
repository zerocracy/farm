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
package com.zerocracy.farm.sync;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.jstk.Farm;
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
 * Test case for {@link SyncFarm}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 */
public final class SyncFarmTest {

    /**
     * SyncFarm can make projects thread safe.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesProjectsThreadSafe() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        final Farm farm = new SyncFarm(new S3Farm(bucket));
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
