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

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import java.io.Closeable;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cactoos.func.RunnableOf;

/**
 * The activity of cleaning the list of claims and processing
 * them all.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class Flush implements Runnable, Closeable {

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
     * @param svc Service
     */
    Flush(final Project pkt, final Collection<Stakeholder> list,
        final ExecutorService svc) {
        this(pkt, new Brigade(list), svc);
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param bgd Brigade
     */
    Flush(final Project pkt, final Brigade bgd) {
        this(pkt, bgd, Executors.newSingleThreadExecutor());
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param bgd Brigade
     * @param svc Service
     */
    Flush(final Project pkt, final Brigade bgd, final ExecutorService svc) {
        this.project = pkt;
        this.brigade = bgd;
        this.service = svc;
        this.alive = new AtomicBoolean();
    }

    @Override
    public void run() {
        if (this.service.isShutdown()) {
            throw new IllegalStateException(
                "The Flush is closed already"
            );
        }
        if (this.service.isShutdown()) {
            throw new IllegalStateException(
                "The Flush is closing now..."
            );
        }
        this.service.submit(
            new RunnableWithTrigger(
                new VerboseRunnable(
                    new RunnableOf<>(
                        new FlushAction(this.project, this.brigade)
                    ),
                    true, true
                ),
                this.alive
            )
        );
    }

    @Override
    public String toString() {
        return this.project.toString();
    }

    @Override
    public void close() {
        while (this.alive.get()) {
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            Logger.info(this, "Waiting for the Flush to close");
        }
        this.service.shutdown();
    }

}
