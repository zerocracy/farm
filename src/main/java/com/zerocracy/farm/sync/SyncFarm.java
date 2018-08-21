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
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.farm.SmartLock;
import com.zerocracy.farm.guts.Guts;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.text.TextOf;
import org.xembly.Directive;
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
     * Pool of locks.
     */
    private final Map<Project, Lock> pool;

    /**
     * Terminator.
     */
    private final Terminator terminator;

    /**
     * Ctor.
     * @param farm Original farm
     */
    public SyncFarm(final Farm farm) {
        // @checkstyle MagicNumber (1 line)
        this(farm, TimeUnit.MINUTES.toMillis(4L));
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param sec Seconds to give to each thread
     */
    public SyncFarm(final Farm farm, final long sec) {
        this.origin = farm;
        this.pool = new ConcurrentHashMap<>(0);
        this.terminator = new Terminator(farm, sec);
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        if (this.pool.size() > Tv.HUNDRED) {
            throw new IllegalStateException(
                String.format(
                    "%s pool overflow, too many items: %d",
                    this.getClass().getCanonicalName(), this.pool.size()
                )
            );
        }
        synchronized (this.origin) {
            return new Guts(
                this.origin,
                () -> new Mapped<>(
                    pkt -> new SyncProject(
                        pkt,
                        this.pool.computeIfAbsent(
                            pkt, p -> new SmartLock()
                        ),
                        this.terminator
                    ),
                    this.origin.find(query)
                ),
                () -> new Directives()
                    .xpath("/guts")
                    .add("farm")
                    .attr("id", this.getClass().getSimpleName())
                    .append(this.terminator.value())
                    .add("locks")
                    .append(
                        new Joined<Directive>(
                            new Mapped<>(
                                ent -> new Directives()
                                    .add("lock")
                                    .attr("pid", ent.getKey().pid())
                                    .attr("label", ent.getValue().toString())
                                    .set(
                                        new TextOf(
                                            ent.getValue().stacktrace()
                                        )
                                    )
                                    .up(),
                                this.pool.entrySet()
                            )
                        )
                    )
                    .up()
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
