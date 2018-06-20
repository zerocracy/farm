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

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.pm.ClaimOut;
import com.zerocracy.pm.Claims;
import com.zerocracy.pmo.Catalog;
import io.sentry.Sentry;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.cactoos.iterable.Shuffled;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * Ping as quartz job.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.21.1
 */
public final class Ping implements Job {
    /**
     * Ping executor.
     */
    private static final Executor EXEC = Executors.newCachedThreadPool();
    /**
     * Farm.
     */
    private final Farm farm;
    /**
     * Ctor.
     * @param frm Farm
     */
    public Ping(final Farm frm) {
        this.farm = frm;
    }

    @Override
    public void execute(final JobExecutionContext ctx) {
        final String claim = ctx.getMergedJobDataMap().getString("claim");
        Ping.EXEC.execute(() -> this.post(claim));
    }

    /**
     * Post a ping.
     * @param type The type of claim to post
     */
    private void post(final String type) {
        try {
            for (final Project project : new Shuffled<>(this.farm.find(""))) {
                this.post(project, type);
            }
        } catch (final IOException err) {
            Sentry.capture(err);
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
