/**
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
package com.zerocracy.farm.reactive;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link DefaultFlush}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (200 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class AsyncFlushTest {

    @Test
    public void processesAllClaims() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        try (final Farm farm = new SyncFarm(new S3Farm(bucket))) {
            final Project project = farm.find("@id='ABCZZFE03'")
                .iterator().next();
            final AtomicInteger done = new AtomicInteger(0);
            final CountDownLatch latch = new CountDownLatch(1);
            final Brigade brigade = new Brigade(
                (Stakeholder) (pkt, claim) -> {
                    done.incrementAndGet();
                    if (new ClaimIn(claim).type().startsWith("nex")) {
                        new ClaimIn(claim).reply("the answer").postTo(project);
                    }
                }
            );
            new ClaimOut().type("first").postTo(project);
            final int max = Tv.FIVE;
            final Thread thread = new Thread(
                new VerboseRunnable(
                    () -> {
                        latch.await();
                        for (int idx = 0; idx < max; ++idx) {
                            new ClaimOut()
                                .token("test;t")
                                .type("next")
                                .param("something", idx)
                                .postTo(project);
                        }
                        return null;
                    }
                )
            );
            thread.start();
            try (final Flush flush =
                new AsyncFlush(new DefaultFlush(brigade))) {
                latch.countDown();
                while (thread.isAlive()) {
                    flush.exec(project);
                }
                thread.join();
                final Claims claims = new Claims(project).bootstrap();
                while (!claims.iterate().isEmpty()) {
                    flush.exec(project);
                }
            }
            MatcherAssert.assertThat(
                done.get(), Matchers.equalTo((max << 1) + 1)
            );
        }
    }

}
