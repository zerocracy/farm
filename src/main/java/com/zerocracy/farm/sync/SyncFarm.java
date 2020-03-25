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
import com.zerocracy.Project;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;

/**
 * Synchronized farm.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "origin")
public final class SyncFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Locks.
     */
    private final Locks locks;

    /**
     * Acquire flags for projects.
     * @checkstyle LineLengthCheck (5 lines)
     */
    private final ConcurrentMap<String, ConcurrentMap<String, AtomicBoolean>> lpkt;

    /**
     * Ctor.
     *
     * @param farm Original farm
     */
    public SyncFarm(final Farm farm) {
        this(farm, new TestLocks());
    }

    /**
     * Ctor.
     *
     * @param farm Original farm
     * @param locks Sync locks
     */
    public SyncFarm(final Farm farm, final Locks locks) {
        this.origin = farm;
        this.locks = locks;
        this.lpkt = new ConcurrentHashMap<>(Tv.FIFTY);
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return new Mapped<>(
            pkt -> new SyncProject(
                pkt, this.locks,
                this.lpkt.computeIfAbsent(
                    pkt.pid(),
                    key -> new ConcurrentHashMap<>(Tv.FIFTY)
                )
            ),
            this.origin.find(query)
        );
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }
}
