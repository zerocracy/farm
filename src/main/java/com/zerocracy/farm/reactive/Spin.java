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
package com.zerocracy.farm.reactive;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.Closeable;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spin that processes all claims in a project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class Spin implements Closeable {

    /**
     * The project.
     */
    private final Project project;

    /**
     * List of stakeholders.
     */
    private final Brigade brigade;

    /**
     * Is it running now?
     */
    private final AtomicBoolean alive;

    /**
     * Service to run.
     */
    private final ExecutorService service;

    /**
     * Ctor.
     * @param pkt Project
     * @param list List of stakeholders
     */
    Spin(final Project pkt, final Collection<Stakeholder> list) {
        this(pkt, new Brigade(list));
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param bgd Brigade
     */
    Spin(final Project pkt, final Brigade bgd) {
        this.project = pkt;
        this.brigade = bgd;
        this.alive = new AtomicBoolean();
        this.service = Executors.newSingleThreadExecutor(
            new VerboseThreads(
                String.format(
                    "spin-%d",
                    new SecureRandom().nextInt(Tv.HUNDRED)
                )
            )
        );
    }

    /**
     * Ping it.
     */
    public void ping() {
        if (!this.alive.get() && !this.service.isShutdown()) {
            this.alive.set(true);
            this.service.submit(
                new VerboseRunnable(
                    new Spinner(this.project, this.brigade, this.alive),
                    true, true
                )
            );
        }
    }

    @Override
    public String toString() {
        return this.project.toString();
    }

    @Override
    public void close() {
        this.service.shutdown();
        while (this.alive.get()) {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
    }

}
