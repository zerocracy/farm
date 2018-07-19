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
package com.zerocracy;

import com.jcabi.aspects.Tv;
import com.zerocracy.farm.fake.FkProject;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.matchers.RunsInThreads;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link Txn}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class TxnTest {

    @Test
    public void commitLocalChangedAndPublishToProject() throws Exception {
        final String file = "test";
        final byte[] payload = {(byte) 42};
        final FkProject pkt = new FkProject();
        try (final Txn txn = new Txn(pkt)) {
            try (final Item item = txn.acq(file)) {
                Files.write(item.path(), payload);
            }
            txn.commit();
        }
        try (final Item item = pkt.acq(file)) {
            MatcherAssert.assertThat(
                Files.readAllBytes(item.path()),
                Matchers.equalTo(payload)
            );
        }
    }

    @Test
    public void deleteLocalChangedOnClose() throws Exception {
        final String file = "empty";
        final byte[] payload = {(byte) 11};
        final FkProject pkt = new FkProject();
        try (final Txn txn = new Txn(pkt)) {
            try (final Item item = txn.acq(file)) {
                Files.write(item.path(), payload);
            }
        }
        try (final Item item = pkt.acq(file)) {
            MatcherAssert.assertThat(
                item.path().toFile().exists(),
                Matchers.is(false)
            );
        }
    }

    /**
     * Tests if {@link Txn} allows concurrent access.
     * @throws Exception If something goes wrong during test.
     * @todo #993:30min Fix NullPointerException due concurrency issues.
     *  Txn.acq() is throwing an NullPointerException when trying to read
     *  this.items. Ignore annotation must be removed after puzzle solution.
     */
    @Test
    @Ignore
    public void allowsConcurrentAccess() throws Exception {
        final byte[] payload = new byte[1];
        final Random rnd = new Random();
        rnd.nextBytes(payload);
        final FkProject pkt = new FkProject();
        MatcherAssert.assertThat(
            t -> {
                final String file = String.format(
                    "test%d",
                    //@checkstyle MagicNumberCheck (1 line)
                    t.getAndIncrement() * rnd.nextInt(1000000000)
                );
                final Txn txn = new Txn(pkt);
                final Item item = txn.acq(file);
                Files.write(item.path(), payload);
                final byte[] arr = Files.readAllBytes(item.path());
                return Arrays.equals(arr, payload);
            },
            new RunsInThreads<>(new AtomicInteger(), Tv.THOUSAND)
        );
    }
}
