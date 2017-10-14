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
package com.zerocracy.farm.sync;

import com.jcabi.log.VerboseThreads;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Mapped;

/**
 * Synchronized farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@EqualsAndHashCode(of = "origin")
public final class SyncFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Pool of locks.
     */
    private final Map<Project, ReentrantLock> pool;

    /**
     * Threshold of locking, in seconds.
     */
    private final long threshold;

    /**
     * Terminator of long running threads.
     */
    private final ExecutorService terminator;

    /**
     * Ctor.
     * @param farm Original farm
     */
    public SyncFarm(final Farm farm) {
        this(farm, TimeUnit.MINUTES.toSeconds(1L));
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param sec Seconds to give to each thread
     */
    public SyncFarm(final Farm farm, final long sec) {
        this.origin = farm;
        this.pool = new ConcurrentHashMap<>(0);
        this.threshold = sec;
        this.terminator = Executors.newCachedThreadPool(
            new VerboseThreads(SyncFarm.class)
        );
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        synchronized (this.origin) {
            return new Mapped<>(
                this.origin.find(query),
                pkt -> new SyncProject(
                    pkt,
                    this.pool.computeIfAbsent(
                        pkt,
                        p -> new ReentrantLock()
                    ),
                    this.threshold,
                    this.terminator
                )
            );
        }
    }

}
