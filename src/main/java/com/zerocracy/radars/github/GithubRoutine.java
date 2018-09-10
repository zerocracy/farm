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
package com.zerocracy.radars.github;

import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtGithub;
import com.zerocracy.sentry.SafeSentry;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.func.IoCheckedProc;

/**
 * GitHub hook, take.
 *
 * @since 1.0
 */
public final class GithubRoutine implements Runnable {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Service.
     */
    private final ScheduledExecutorService service;

    /**
     * Ctor.
     * @param farm Farm
     */
    public GithubRoutine(final Farm farm) {
        this.farm = farm;
        this.service = Executors.newSingleThreadScheduledExecutor(
            new VerboseThreads(GithubRoutine.class)
        );
    }

    /**
     * Start it.
     */
    public void start() {
        this.service.scheduleWithFixedDelay(
            new VerboseRunnable(this, true, true),
            1L, 1L, TimeUnit.MINUTES
        );
    }

    @Override
    public void run() {
        try {
            new IoCheckedProc<>(
                new AcceptInvitations(new ExtGithub(this.farm).value())
            ).exec(true);
        } catch (final IOException err) {
            new SafeSentry(this.farm).capture(err);
        }
    }
}
