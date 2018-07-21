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
package com.zerocracy.pm;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.mongodb.client.model.Filters;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.entry.ExtMongo;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.farm.sync.SyncFarm;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link Footprint}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class FootprintTest {

    @Test
    public void addsClaims() throws Exception {
        final Farm farm = new PropsFarm();
        final Project project = farm.find("@id='FOOTPRNTX'")
            .iterator().next();
        new ClaimOut().type("Hello").postTo(project);
        final XML xml = new Claims(project).iterate().iterator().next();
        try (
            final Footprint footprint = FootprintTest.footprint(farm, project)
        ) {
            footprint.open(xml);
            footprint.close(xml);
            footprint.cleanup(new Date());
            MatcherAssert.assertThat(
                footprint.collection().find(
                    Filters.eq("project", project.pid())
                ),
                Matchers.iterableWithSize(1)
            );
        }
    }

    @Test
    @Ignore
    // @todo #1050:30min This test is not stable (depending on environment it
    //  fails in about 5% cases) refactor it to make it stable. Remember to run
    //  large number of builds to make sure it doesn't fail (at least 100
    //  builds are needed, maybe more). The trouble is most probably in ExtMongo
    //  for the fake Mongo instance.
    public void addsInThreads() throws Exception {
        try (final Farm farm = new SyncFarm(new PropsFarm())) {
            MatcherAssert.assertThat(
                inc -> {
                    final Project project = farm.find(
                        String.format("@id='%09d'", inc.incrementAndGet())
                    ).iterator().next();
                    new ClaimOut().type("Version").postTo(project);
                    final XML xml = new Claims(project)
                        .iterate().iterator().next();
                    try (
                        final Footprint footprint =
                            FootprintTest.footprint(farm, project)
                    ) {
                        footprint.open(xml);
                        footprint.close(xml);
                        return footprint.collection().find(
                            Filters.eq("project", project.pid())
                        ).iterator().hasNext();
                    }
                },
                new RunsInThreads<>(new AtomicInteger())
            );
        }
    }

    @Test
    public void cleanOldClaims() throws Exception {
        final Farm farm = new PropsFarm();
        final Project project = farm.find("@id='FOOTPRNTY'")
            .iterator().next();
        new ClaimOut(new Date(0L)).type("Notify").postTo(project);
        final XML xml = new Claims(project).iterate().iterator().next();
        try (
            final Footprint footprint = FootprintTest.footprint(farm, project)
        ) {
            footprint.open(xml);
            footprint.close(xml);
            MatcherAssert.assertThat(
                footprint.cleanup(
                    new Date(TimeUnit.DAYS.toMillis((long) Tv.HUNDRED))
                ),
                Matchers.equalTo(1L)
            );
            MatcherAssert.assertThat(
                footprint.collection().find(
                    Filters.eq("project", project.pid())
                ),
                Matchers.emptyIterable()
            );
        }
    }

    /**
     * Create footprint with custom Mongo DB.
     * @param farm Farm to use
     * @param project Project to use
     * @return Created footprint
     * @throws IOException In case of error
     */
    private static Footprint footprint(final Farm farm, final Project project)
        throws IOException {
        return new Footprint(
            new ExtMongo(farm, UUID.randomUUID().toString()).value(),
            project.pid()
        );
    }
}
