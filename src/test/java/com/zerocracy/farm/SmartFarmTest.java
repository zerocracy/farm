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
package com.zerocracy.farm;

import com.jcabi.log.VerboseRunnable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.fake.FkBucket;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.props.Props;
import com.zerocracy.pm.cost.Boosts;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.func.RunnableOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SmartFarm}.
 * @since 1.0
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
        try (final Farm farm = new SmartFarm(new S3Farm(bucket))) {
            final Project project = farm.find("@id='SMRTFRMTT'")
                .iterator().next();
            MatcherAssert.assertThat(
                inc -> {
                    final String job = String.format(
                        "gh:test/test#%d", inc.incrementAndGet()
                    );
                    new Wbs(project).bootstrap().add(job);
                    new Boosts(project).bootstrap().boost(job, 1);
                    new Wbs(project).bootstrap().remove(job);
                    return new Boosts(project).factor(job) == 2;
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }

    @Test
    public void synchronizesBetweenProjects() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-again"
        );
        try (final Farm farm = new SmartFarm(new S3Farm(bucket))) {
            MatcherAssert.assertThat(
                inc -> {
                    final Project project = farm.find("@id='AAAAABBBB'")
                        .iterator().next();
                    final String job = String.format(
                        "gh:test/testing#%d", inc.incrementAndGet()
                    );
                    new Wbs(project).bootstrap().add(job);
                    return new Wbs(project).exists(job);
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }

    @Test
    public void noConflictsBetweenProjects() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-3"
        );
        try (final Farm farm = new SmartFarm(new S3Farm(bucket))) {
            MatcherAssert.assertThat(
                inc -> {
                    final Project project = farm.find(
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

    @Test
    public void returnsSameProject() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-6"
        );
        try (final Farm farm = new SmartFarm(new S3Farm(bucket))) {
            final Project project = farm.find(
                "@id='123456709'"
            ).iterator().next();
            final String job = "gh:test/test#2989";
            new Wbs(project).bootstrap().add(job);
            MatcherAssert.assertThat(
                new Wbs(project).bootstrap().iterate(),
                Matchers.hasItem(job)
            );
        }
    }

    @Test
    public void readsProps() throws Exception {
        try (final Farm farm = new SmartFarm(new FkFarm())) {
            final Project project = farm.find(
                "@id='123456700'"
            ).iterator().next();
            MatcherAssert.assertThat(
                new Props(project).has("//testing"),
                Matchers.equalTo(true)
            );
        }
    }

    @Test
    public void preservesConsistency() throws Exception {
        final Bucket bucket = new FkBucket(
            Files.createTempDirectory("").toFile(),
            "some-bucket-09"
        );
        try (final Farm farm = new SmartFarm(new S3Farm(bucket))) {
            final Project project = farm.find(
                "@id='SMARTFARM'"
            ).iterator().next();
            final String job = "gh:test/test#22";
            new Wbs(project).bootstrap().add(job);
            new Orders(project).bootstrap().assign(job, "jeff", 0L);
            new VerboseRunnable(
                new RunnableOf<>(
                    obj -> {
                        new Wbs(project).bootstrap().remove(job);
                    }
                ),
                true, false
            ).run();
            MatcherAssert.assertThat(
                new Wbs(project).bootstrap().exists(job),
                Matchers.equalTo(true)
            );
        }
    }

}
