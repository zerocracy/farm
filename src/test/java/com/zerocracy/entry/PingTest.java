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
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.ClaimsItem;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.time.DateAsText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.xembly.Directives;

/**
 * Tests for {@link Ping}.
 * @since 1.0
 * @todo #1200:30min Add a test that validates that pings are sent to projects
 *  in batches depending on the ping interval. All projects need to be pinged
 *  eventually.
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class PingTest {

    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void worksForSingleProject() throws Exception {
        final FkProject pkt = new FkProject();
        final FkFarm farm = new FkFarm(pkt);
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
        final JobDataMap map = new JobDataMap();
        final String type = "Ping";
        map.put("claim", type);
        final AtomicInteger counter = new AtomicInteger(0);
        final int batches = Tv.FIVE;
        for (int count = 0; count < batches; ++count) {
            new Ping(farm, batches).execute(this.context(map, counter));
        }
        final XML xml = new ClaimsItem(pkt).iterate().iterator().next();
        MatcherAssert.assertThat(
            new ClaimsItem(pkt).bootstrap().iterate(),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
            new ClaimIn(xml).type(),
            Matchers.is(type)
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
}
