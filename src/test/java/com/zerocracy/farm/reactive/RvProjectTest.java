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
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.RunsInThreads;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.farm.sync.SyncFarm;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pmo.Pmo;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.list.SolidList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RvProject}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class RvProjectTest {

    @Test
    public void closesClaims() throws Exception {
        final AtomicInteger done = new AtomicInteger();
        try (final Farm farm = new SyncFarm(new FkFarm())) {
            final Project raw = new Pmo(farm);
            final Flush def = new DefaultFlush(
                new Brigade(
                    new SolidList<>(
                        (project, xml) -> done.incrementAndGet()
                    )
                )
            );
            try (final Flush flush = new AsyncFlush(def)) {
                final Project project = new RvProject(raw, flush);
                final Claims claims = new Claims(project).bootstrap();
                claims.add(new ClaimOut().type("hello A").token("test;t1"));
                claims.add(new ClaimOut().type("hello B").token("test;t2"));
                while (true) {
                    if (!claims.iterate().iterator().hasNext()) {
                        break;
                    }
                }
            }
            MatcherAssert.assertThat(done.get(), Matchers.equalTo(2));
        }
    }

    @Test
    public void closesClaimsInThreads() throws Exception {
        final AtomicInteger total = new AtomicInteger(Tv.FIFTY);
        try (final Farm farm = new SyncFarm(new FkFarm())) {
            final Project raw = new Pmo(farm);
            final Flush def = new DefaultFlush(
                new Brigade(
                    new SolidList<>(
                        (project, xml) -> total.decrementAndGet()
                    )
                )
            );
            try (final Flush flush = new AsyncFlush(def)) {
                final Project project = new RvProject(raw, flush);
                MatcherAssert.assertThat(
                    input -> {
                        new ClaimOut()
                            .type("hello you")
                            .param("something", input.incrementAndGet())
                            .postTo(project);
                        return true;
                    },
                    new RunsInThreads<>(new AtomicInteger(), total.get())
                );
                final Claims claims = new Claims(project).bootstrap();
                while (true) {
                    if (!claims.iterate().iterator().hasNext()) {
                        break;
                    }
                }
            }
            MatcherAssert.assertThat(total.get(), Matchers.equalTo(0));
        }
    }

}
