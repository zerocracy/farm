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

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseThreads;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Func;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SyncFarm}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
public final class SyncFarmTest {

    @Test
    public void makesProjectsThreadSafe() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        final Farm farm = new SyncFarm(new S3Farm(bucket));
        final int threads =
            Runtime.getRuntime().availableProcessors() << Tv.FOUR;
        final ExecutorService service = Executors.newFixedThreadPool(
            threads, new VerboseThreads()
        );
        final CountDownLatch latch = new CountDownLatch(1);
        final Collection<Future<Boolean>> futures = new ArrayList<>(threads);
        final Project project = farm.find("@id='ABCZZFE03'").iterator().next();
        final Roles roles = new Roles(project);
        final String role = "PO";
        final AtomicInteger inc = new AtomicInteger();
        final Callable<Boolean> job = () -> {
            latch.await();
            final String person = String.format(
                "jeff%d", inc.incrementAndGet()
            );
            final String task = String.format(
                "gh:test/test#%d", inc.incrementAndGet()
            );
            new Wbs(project).bootstrap().add(task);
            roles.bootstrap().assign(person, role);
            return roles.hasRole(person, role);
        };
        for (int thread = 0; thread < threads; ++thread) {
            futures.add(service.submit(job));
        }
        latch.countDown();
        MatcherAssert.assertThat(
            new And(
                futures,
                (Func<Future<Boolean>, Boolean>) Future::get
            ).value(),
            Matchers.is(true)
        );
    }

}
