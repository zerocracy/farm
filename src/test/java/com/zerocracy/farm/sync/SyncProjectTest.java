/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.props.PropsFarm;
import com.zerocracy.pmo.Pmo;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SyncProject}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class SyncProjectTest {

    @Test
    public void locksFilesIndividually() throws Exception {
        try (final Farm farm = new SyncFarm(new PropsFarm())) {
            final Project project = new Pmo(farm);
            final Collection<Item> items = new LinkedList<>();
            for (int idx = 0; idx < Tv.FIFTY; ++idx) {
                final Item item = project.acq(String.format("%d.xml", 0));
                item.path();
                items.add(item);
            }
            for (final Item item : items) {
                item.close();
            }
            MatcherAssert.assertThat(
                items.size(),
                Matchers.greaterThan(0)
            );
        }
    }

    @Test
    public void unlockIfHoldTooLong() throws Exception {
        boolean interrupted = false;
        final TestLocks locks = new TestLocks();
        try (final Farm farm = new SyncFarm(new PropsFarm(), locks, 1L)) {
            final Project pkt = new Pmo(farm);
            try (final Item item = pkt.acq("test.txt")) {
                item.path();
                TimeUnit.SECONDS.sleep(2L);
            } catch (final InterruptedException iex) {
                interrupted = true;
            }
            MatcherAssert.assertThat(
                "Interrupted exception",
                interrupted, Matchers.is(true)
            );
            MatcherAssert.assertThat(
                "Thread interrupted flag",
                Thread.currentThread().isInterrupted(),
                Matchers.is(false)
            );
        }
    }
}
