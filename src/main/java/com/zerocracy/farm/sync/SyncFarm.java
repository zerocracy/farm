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
import com.zerocracy.farm.guts.Guts;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;
import org.xembly.Directives;

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
     * Terminator.
     */
    private final Terminator terminator;

    /**
     * Locks.
     */
    private final Locks locks;

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
     * @param farm Farm
     * @param locks Locks
     */
    public SyncFarm(final Farm farm, final Locks locks) {
        this(farm, locks, TimeUnit.MINUTES.toMillis((long) Tv.FOUR));
    }

    /**
     * Ctor.
     *
     * @param farm Original farm
     * @param locks Sync locks
     * @param sec Seconds to give to each thread
     */
    public SyncFarm(final Farm farm, final Locks locks, final long sec) {
        this.origin = farm;
        this.locks = locks;
        this.terminator = new Terminator(farm, sec);
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        synchronized (this.origin) {
            return new Guts(
                this.origin,
                () -> new Mapped<>(
                    pkt -> new SyncProject(pkt, this.locks, this.terminator),
                    this.origin.find(query)
                ),
                () -> new Directives()
                    .xpath("/guts")
                    .add("farm")
                    .attr("id", this.getClass().getSimpleName())
                    .append(this.terminator.value())
            ).apply(query);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.terminator.close();
        } finally {
            this.origin.close();
        }
    }
}
