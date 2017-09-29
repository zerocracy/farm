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

import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.RunsInThreads;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Scalar;
import org.cactoos.iterable.PropertiesOf;
import org.cactoos.map.MapEntry;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link SmartFarm}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class SmartFarmTest {

    @Test
    public void worksInManyThreads() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket"
        );
        final Farm farm = new SmartFarm(
            new S3Farm(bucket),
            new PropertiesOf(new MapEntry<>("testing", "true")).value(),
            new HashMap<>(0)
        ).value();
        final Project project = farm.find("@id='SMRTFRMTT'").iterator().next();
        MatcherAssert.assertThat(
            inc -> {
                final String job = String.format(
                    "gh:test/test#%d", inc.incrementAndGet()
                );
                new Wbs(project).bootstrap().add(job);
                new Orders(project).bootstrap().assign(job, "yegor", "reason");
                new Wbs(project).bootstrap().remove(job);
                return !new Orders(project).assigned(job);
            },
            new RunsInThreads<>(new AtomicInteger())
        );
    }

    @Test
    public void synchronizesBetweenProjects() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-again"
        );
        final Scalar<Farm> farm = new SmartFarm(
            new S3Farm(bucket), new Properties(), new HashMap<>(0)
        );
        MatcherAssert.assertThat(
            inc -> {
                final Project project = farm.value()
                    .find("@id='AAAAABBBB'").iterator().next();
                final String job = String.format(
                    "gh:test/testing#%d", inc.incrementAndGet()
                );
                new Wbs(project).bootstrap().add(job);
                return new Wbs(project).exists(job);
            },
            new RunsInThreads<>(new AtomicInteger())
        );
    }

    @Test
    public void noConflictsBetweenProjects() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-3"
        );
        final Scalar<Farm> farm = new SmartFarm(
            new S3Farm(bucket), new Properties(), new HashMap<>(0)
        );
        MatcherAssert.assertThat(
            inc -> {
                final Project project = farm.value().find(
                    String.format("@id='AAAAA%04d'", inc.incrementAndGet())
                ).iterator().next();
                final String job = "gh:test/some#2";
                new Wbs(project).bootstrap().add(job);
                return new Wbs(project).iterate().size() == 1;
            },
            new RunsInThreads<>(new AtomicInteger())
        );
    }

}
