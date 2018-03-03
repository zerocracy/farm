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
package com.zerocracy.entry;

import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pmo.Catalog;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.Proc;
import org.cactoos.func.RunnableOf;
import org.cactoos.iterable.Shuffled;

/**
 * Pings.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.21
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class Pings {

    /**
     * Executor service.
     */
    private final ScheduledExecutorService executor;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param frm Farm
     */
    Pings(final Farm frm) {
        this.farm = frm;
        this.executor = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new VerboseThreads(Pings.class)
        );
    }

    /**
     * Start it.
     */
    public void start() {
        this.executor.scheduleWithFixedDelay(
            new VerboseRunnable(
                new RunnableOf<>(
                    (Proc<Void>) input -> this.post("Ping")
                ),
                true, true
            ),
            1L, 1L, TimeUnit.MINUTES
        );
        this.executor.scheduleWithFixedDelay(
            new VerboseRunnable(
                new RunnableOf<>(
                    (Proc<Void>) input -> this.post("Ping hourly")
                ),
                true, true
            ),
            1L, 1L, TimeUnit.HOURS
        );
        this.executor.scheduleWithFixedDelay(
            new VerboseRunnable(
                new RunnableOf<>(
                    (Proc<Void>) input -> this.post("Ping daily")
                ),
                true, true
            ),
            1L, 1L, TimeUnit.DAYS
        );
    }

    /**
     * Post a ping.
     * @param type The type of claim to post
     * @throws IOException If fails
     */
    private void post(final String type) throws IOException {
        for (final Project project : new Shuffled<>(this.farm.find(""))) {
            this.post(project, type);
        }
    }

    /**
     * Post a ping.
     * @param project The project
     * @param type The type of claim to post
     * @throws IOException If fails
     */
    private void post(final Project project, final String type)
        throws IOException {
        final Catalog catalog = new Catalog(this.farm).bootstrap();
        if (catalog.exists(project.pid()) && !catalog.pause(project.pid())) {
            final Claims claims = new Claims(project).bootstrap();
            if (claims.iterate().isEmpty()) {
                new ClaimOut().type(type).postTo(project);
            }
        }
    }

}
