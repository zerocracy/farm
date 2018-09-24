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
package com.zerocracy.farm.footprint;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.Footprint;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.sync.SyncFarm;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FtFarm}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
public final class FtFarmTest {

    @Test
    public void recordsChangesToClaims() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        try (final Farm farm = new FtFarm(
            new PropsFarm(new SyncFarm(new S3Farm(bucket)))
        )) {
            final String pid = "ABCZZTY03";
            final Project project = farm.find(
                String.format("@id='%s'", pid)
            ).iterator().next();
            final AtomicLong cid = new AtomicLong(1L);
            final int threads = 10;
            MatcherAssert.assertThat(
                inc -> {
                    final long num = cid.getAndIncrement();
                    new ClaimOut().cid(Long.toString(num))
                        .type("Hello")
                        .param("something", num)
                        .author("0pdd")
                        .postTo(new ClaimsOf(farm, project));
                    return true;
                },
                new RunsInThreads<>(new AtomicInteger(), threads)
            );
            try (final Footprint footprint = new Footprint(farm, project)) {
                MatcherAssert.assertThat(
                    footprint.collection().find(Filters.eq("project", pid)),
                    Matchers.iterableWithSize(threads)
                );
            }
        }
    }

}
