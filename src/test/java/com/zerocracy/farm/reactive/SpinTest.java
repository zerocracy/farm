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
package com.zerocracy.farm.reactive;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.jstk.fake.FkProject;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Spin}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class SpinTest {

    /**
     * Spin can work.
     * @throws Exception If some problem inside
     */
    @Test
    public void processes() throws Exception {
        final Project project = new FkProject();
        final AtomicInteger total = new AtomicInteger(Tv.TEN);
        final CountDownLatch latch = new CountDownLatch(1);
        final Brigade brigade = new Brigade(
            (Stakeholder) (pkt, claim) -> total.decrementAndGet()
        );
        new ClaimOut().type("first").postTo(project);
        final Thread thread = new Thread(
            new VerboseRunnable(
                () -> {
                    final int max = total.get();
                    latch.await();
                    for (int idx = 0; idx < max; ++idx) {
                        new ClaimOut().type("next").postTo(project);
                    }
                    return null;
                }
            )
        );
        thread.start();
        try (final Spin spin = new Spin(project, brigade)) {
            latch.countDown();
            spin.ping();
        }
        thread.join();
        try (final Claims claims = new Claims(project).lock()) {
            MatcherAssert.assertThat(
                claims.iterate(),
                Matchers.hasSize(0)
            );
        }
        MatcherAssert.assertThat(total.get(), Matchers.equalTo(0));
    }

}
