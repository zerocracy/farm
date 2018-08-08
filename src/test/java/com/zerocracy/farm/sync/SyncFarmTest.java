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
package com.zerocracy.farm.sync;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import com.zerocracy.pmo.Pmo;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.func.RunnableOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SyncFarm}.
 * @since 1.0
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
        try (final Farm farm = new SyncFarm(new S3Farm(bucket))) {
            final Project project = farm.find("@id='ABCZZFE03'")
                .iterator().next();
            final Roles roles = new Roles(project);
            final String role = "QA";
            MatcherAssert.assertThat(
                inc -> {
                    final String person = String.format(
                        "jeff%d", inc.incrementAndGet()
                    );
                    final String task = String.format(
                        "gh:test/test#%d", inc.incrementAndGet()
                    );
                    new Wbs(project).bootstrap().add(task);
                    roles.bootstrap().assign(person, role);
                    return roles.hasRole(person, role);
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }

    @Test
    public void interruptsTooLongThread() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket-1"
        );
        try (final Farm farm = new SyncFarm(
            new S3Farm(bucket), TimeUnit.SECONDS.toMillis(1L)
        )) {
            final Project pmo = new Pmo(farm);
            final CountDownLatch locked = new CountDownLatch(1);
            new Thread(
                new RunnableOf<Object>(
                    input -> {
                        try (final Item item = pmo.acq("expectedfailure.xml")) {
                            MatcherAssert.assertThat(
                                item.path(), Matchers.notNullValue()
                            );
                            locked.countDown();
                            TimeUnit.MINUTES.sleep(1L);
                        }
                    }
                )
            ).start();
            locked.await();
            try (final Item item = pmo.acq("a.xml")) {
                MatcherAssert.assertThat(
                    item.path(),
                    Matchers.notNullValue()
                );
            }
            try (final Item item = pmo.acq("c.xml")) {
                MatcherAssert.assertThat(
                    item.path(),
                    Matchers.notNullValue()
                );
            }
        }
    }
}
