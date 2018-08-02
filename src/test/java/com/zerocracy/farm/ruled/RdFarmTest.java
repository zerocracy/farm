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
package com.zerocracy.farm.ruled;

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.entry.ClaimsOf;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.strict.StrictFarm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pmo.Pmo;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RdFarm}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RdFarmTest {

    @Test
    public void worksInManyThreads() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        try (final Farm farm = new SyncFarm(new RdFarm(new S3Farm(bucket)))) {
            final Project project = farm.find("@id='RDFRMTEST'")
                .iterator().next();
            final Wbs wbs = new Wbs(project).bootstrap();
            final AtomicInteger total = new AtomicInteger();
            MatcherAssert.assertThat(
                inc -> {
                    final String job = String.format(
                        "gh:test/test#%d", inc.incrementAndGet()
                    );
                    wbs.add(job);
                    return wbs.exists(job);
                },
                new RunsInThreads<>(total)
            );
            MatcherAssert.assertThat(
                wbs.iterate().size(),
                Matchers.equalTo(total.get())
            );
        }
    }

    @Test
    public void worksWithPmo() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-pmo"
        );
        try (
            final Farm farm =
                new RdFarm(new StrictFarm(new PropsFarm(new S3Farm(bucket))))
        ) {
            final Project pmo = new Pmo(farm);
            new ClaimOut().type("hello you").postTo(new ClaimsOf(farm));
            MatcherAssert.assertThat(
                new ClaimsItem(pmo).iterate().iterator().hasNext(),
                Matchers.equalTo(true)
            );
        }
    }

}
