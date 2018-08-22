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
package com.zerocracy.entry;

import com.jcabi.aspects.Tv;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.pmo.Catalog;
import com.zerocracy.sentry.SafeSentry;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.list.ListOf;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Ping as quartz job.
 * @since 1.0
 */
public final class Ping implements Job {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Batch size.
     */
    private final int batches;

    /**
     * Ctor.
     * @param frm Farm
     * @param btchs Number of batches for minute pings
     */
    public Ping(final Farm frm, final int btchs) {
        this.farm = frm;
        this.batches = btchs;
    }

    @Override
    public void execute(final JobExecutionContext ctx)
        throws JobExecutionException {
        try {
            this.post(
                ctx.getMergedJobDataMap().getString("claim"),
                (AtomicInteger) ctx.getScheduler().getContext().get("counter")
            );
        } catch (final SchedulerException err) {
            throw new JobExecutionException(
                "Failed to obtain job counter",
                err
            );
        } catch (final IOException | IllegalStateException err) {
            new SafeSentry(this.farm).capture(err);
            final JobExecutionException exx =
                new JobExecutionException("Failed to execute a job", err);
            exx.setRefireImmediately(true);
            throw exx;
        }
    }

    /**
     * Post a ping.
     * @param type The type of claim to post
     * @param counter Counter of pings
     * @throws IOException If fails
     */
    private void post(final String type,
        final AtomicInteger counter) throws IOException {
        final Iterable<Project> projects;
        if (Objects.equals(type, "Ping")) {
            projects = this.batch(counter);
        } else {
            projects = this.farm.find("");
        }
        for (final Project project : projects) {
            this.post(project, type);
        }
    }

    /**
     * Get project list for ping claim.
     * @param counter Counter of pings
     * @return List of projects to ping
     * @throws IOException In case of error
     */
    private Iterable<Project> batch(final AtomicInteger counter)
        throws IOException {
        final int batch = counter.getAndIncrement() % this.batches;
        final List<Project> projects = new ListOf<>(this.farm.find(""));
        final double size = (double) projects.size() / this.batches;
        return projects.subList(
            (int) (batch * size),
            (int) Math.min(projects.size(), (batch + 1) * size)
        );
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
            new ClaimOut()
                .type(type)
                .postTo(
                    new ClaimsOf(this.farm, project),
                    Instant.now().plus(Duration.ofMinutes(Tv.FIVE))
                );
        }
    }
}
