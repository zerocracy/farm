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

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.S3Farm;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pm.cost.Boosts;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pmo.Pmo;
import java.nio.file.Files;
import org.cactoos.func.RunnableOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RdItem}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RdItemTest {

    @Test(expected = IllegalStateException.class)
    public void catchesIllegalModification() throws Exception {
        try (final Farm farm = new RdFarm(new FkFarm())) {
            final Project pmo = new Pmo(farm);
            new Boosts(pmo).bootstrap();
            new Boosts(pmo).boost("gh:test/test#509", 1);
        }
    }

    @Test
    public void ignoresNonXmlFiles() throws Exception {
        try (final Farm farm = new RdFarm(new FkFarm())) {
            final Project pmo = new Pmo(farm);
            try (final Item item = pmo.acq("test.txt")) {
                Files.write(item.path(), "How are you, dude?".getBytes());
            }
        }
    }

    @Test
    public void rejectsInvalidChanges() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket-1"
        );
        try (final Farm farm = new RdFarm(new SyncFarm(new S3Farm(bucket)))) {
            final Project pkt = farm.find("@id='ABCDEDDHI'").iterator().next();
            final String job = "gh:test/test#55";
            new VerboseRunnable(
                new RunnableOf<>(
                    input -> {
                        new Boosts(pkt).bootstrap().boost(job, 1);
                    }
                ),
                true, false
            ).run();
            MatcherAssert.assertThat(
                new Boosts(pkt).bootstrap().factor(job),
                Matchers.equalTo(2)
            );
        }
    }

    @Test
    public void closesClaims() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket"
        );
        try (final Farm farm = new RdFarm(new SyncFarm(new S3Farm(bucket)))) {
            final Project pkt = farm.find("@id='ABCDEFGHI'").iterator().next();
            final String first = "gh:test/test#1";
            new Wbs(pkt).bootstrap().add(first);
            final String second = "gh:test/test#2";
            new Wbs(pkt).bootstrap().add(second);
            new Boosts(pkt).bootstrap().boost(first, Tv.TEN);
            new Boosts(pkt).bootstrap().boost(second, Tv.TEN);
            new Wbs(pkt).remove(first);
            MatcherAssert.assertThat(
                new Boosts(pkt).factor(first),
                Matchers.not(Matchers.equalTo(Tv.TEN))
            );
            MatcherAssert.assertThat(
                new Boosts(pkt).factor(second),
                Matchers.equalTo(Tv.TEN)
            );
        }
    }

    @Test
    public void closesUnderlyingItemAnyway() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "the-bucket-98"
        );
        try (final Farm farm = new RdFarm(new SyncFarm(new S3Farm(bucket)))) {
            final Project pkt = farm.find("@id='ABCDTTGHI'").iterator().next();
            final String job = "gh:test/test#143";
            final Thread bug = new Thread(
                new RunnableOf<Object>(
                    input -> {
                        new Boosts(pkt).bootstrap().boost(job, Tv.TEN);
                    }
                )
            );
            bug.start();
            bug.join();
            new Boosts(pkt).bootstrap();
        }
    }

}
