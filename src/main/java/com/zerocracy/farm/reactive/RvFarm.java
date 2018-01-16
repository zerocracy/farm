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

import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.ShutUp;
import com.zerocracy.Stakeholder;
import com.zerocracy.farm.guts.Guts;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import org.cactoos.func.RunnableOf;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.SolidScalar;
import org.cactoos.scalar.UncheckedScalar;
import org.xembly.Directives;

/**
 * Reactive farm.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class RvFarm implements Farm {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Flush.
     */
    private final Flush flush;

    /**
     * Every minute flusher.
     */
    private final UncheckedScalar<ExecutorService> routine;

    /**
     * Ctor.
     * @param farm Original farm
     */
    public RvFarm(final Farm farm) {
        this(farm, new Brigade());
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param list List of stakeholders
     */
    public RvFarm(final Farm farm, final Iterable<Stakeholder> list) {
        this(farm, new Brigade(list));
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param bgd Stakeholders
     */
    public RvFarm(final Farm farm, final Brigade bgd) {
        this(farm, bgd, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param bgd Stakeholders
     * @param threads How many threads to use
     */
    public RvFarm(final Farm farm, final Brigade bgd, final int threads) {
        this(farm, new AsyncFlush(new DefaultFlush(bgd), threads));
    }

    /**
     * Ctor.
     * @param farm Original farm
     * @param flsh Flush
     */
    public RvFarm(final Farm farm, final Flush flsh) {
        this.origin = farm;
        this.flush = flsh;
        this.routine = new UncheckedScalar<>(
            new SolidScalar<>(
                () -> {
                    final ScheduledExecutorService svc =
                        Executors.newSingleThreadScheduledExecutor(
                            new VerboseThreads(RvFarm.class)
                        );
                    svc.scheduleWithFixedDelay(
                        new VerboseRunnable(
                            new RunnableOf<Boolean>(
                                input -> {
                                    for (final Project pkt
                                        : this.origin.find("")) {
                                        this.flush.exec(pkt);
                                    }
                                }
                            ),
                            true, true
                        ),
                        1L, 1L, TimeUnit.SECONDS
                    );
                    return svc;
                }
            )
        );
    }

    @Override
    public Iterable<Project> find(final String query) throws IOException {
        return new Guts(
            this.origin,
            () -> new Mapped<>(
                pkt -> new RvProject(pkt, this.flush),
                this.origin.find(query)
            ),
            () -> new Directives()
                .xpath("/guts")
                .add("farm")
                .attr("id", this.getClass().getSimpleName())
                .append(this.flush.value())
        ).apply(query);
    }

    @Override
    public void close() throws IOException {
        new ShutUp(this.routine.value()).close();
        try {
            this.flush.close();
        } finally {
            this.origin.close();
        }
    }
}
