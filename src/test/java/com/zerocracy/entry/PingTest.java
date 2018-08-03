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
package com.zerocracy.entry;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Catalog;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.text.FormattedText;
import org.cactoos.time.DateAsText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.xembly.Directives;

/**
 * Tests for {@link Ping}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public final class PingTest {

    /**
     * Ping type.
     */
    private static final String PING = "Ping";

    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void worksForSingleProject() throws Exception {
        final FkProject pkt = new FkProject();
        final Farm farm = new PropsFarm(new FkFarm(pkt));
        try (final Item item = pkt.acq("catalog.xml")) {
            new Xocument(item.path()).bootstrap("pmo/catalog");
            new Xocument(item.path()).modify(
                new Directives()
                    .xpath("/catalog")
                    .add("project")
                    .attr("id", pkt.pid())
                    .add("title").set(pkt.pid()).up()
                    .add("created")
                    .set(new DateAsText().asString()).up()
                    .add("prefix").set("2017/01/AAAABBBBC/").up()
                    .add("fee").set("0").up()
                    .add("alive").set("true").up()
                    .add("publish").set("false").up()
                    .add("adviser").set("0crat").up()
            );
        }
        final AtomicInteger counter = new AtomicInteger(0);
        final int batches = Tv.FIVE;
        for (int count = 0; count < batches; ++count) {
            new Ping(farm, batches)
                .execute(this.context(PingTest.map(), counter));
        }
        final XML xml = new ClaimsItem(pkt).iterate().iterator().next();
        MatcherAssert.assertThat(
            new ClaimsItem(pkt).bootstrap().iterate(),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
            new ClaimIn(xml).type(),
            Matchers.is(PingTest.PING)
        );
    }

    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void worksForManyProjects() throws Exception {
        final List<Project> projects = new LinkedList<>();
        final int batches = Tv.FIVE;
        for (int ident = 0; ident < batches; ++ident) {
            // @checkstyle MagicNumber (1 line)
            final String pid = String.valueOf(100000000 + ident);
            projects.add(new FkProject(pid));
        }
        final Farm farm = new PropsFarm(s -> projects);
        final Catalog catalog = new Catalog(farm).bootstrap();
        for (final Project pkt : projects) {
            catalog.add(
                pkt.pid(),
                new FormattedText("2017/01/%s/", pkt.pid()).asString()
            );
        }
        final AtomicInteger counter = new AtomicInteger(0);
        final Ping ping = new Ping(farm, batches);
        final JobExecutionContext context =
            this.context(PingTest.map(), counter);
        for (int count = 0; count < batches; ++count) {
            ping.execute(context);
            final Project pkt = projects.get(count);
            final XML xml = new ClaimsItem(pkt).iterate().iterator().next();
            MatcherAssert.assertThat(
                new ClaimsItem(pkt).bootstrap().iterate(),
                Matchers.hasSize(1)
            );
            MatcherAssert.assertThat(
                new ClaimIn(xml).type(),
                Matchers.is(PingTest.PING)
            );
        }
    }

    @Test(expected = JobExecutionException.class)
    public void throwJobExecutionExceptionOnClaimFail() throws Exception {
        new Ping(
            xpath -> {
                throw new IllegalStateException("Farm error");
            },
            1
        ).execute(
            this.context(
                PingTest.map(),
                new AtomicInteger()
            )
        );
    }

    private JobExecutionContext context(final JobDataMap map,
        final AtomicInteger counter)
        throws SchedulerException {
        final JobExecutionContext ctx = Mockito.mock(JobExecutionContext.class);
        Mockito.when(ctx.getMergedJobDataMap()).thenReturn(map);
        final Scheduler scheduler = Mockito.mock(Scheduler.class);
        Mockito.when(ctx.getScheduler()).thenReturn(scheduler);
        final SchedulerContext sctx = new SchedulerContext();
        sctx.put("counter", counter);
        Mockito.when(scheduler.getContext()).thenReturn(sctx);
        return ctx;
    }

    private static JobDataMap map() {
        return new JobDataMap(
            new MapOf<String, String>(
                new MapEntry<>("claim", PingTest.PING)
            )
        );
    }
}
