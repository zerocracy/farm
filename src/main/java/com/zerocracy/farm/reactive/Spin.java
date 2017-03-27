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
import com.jcabi.log.VerboseThreads;
import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.Claims;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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
final class Spin implements Runnable, Closeable {

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
            new VerboseThreads("spin")
        );
    }

    /**
     * Ping it.
     */
    public void ping() {
        if (!this.alive.get() && !this.service.isShutdown()) {
            this.alive.set(true);
            this.service.submit(new VerboseRunnable(this, true, true));
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

    @Override
    public void run() {
        try {
            this.process();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        this.alive.set(false);
    }

    /**
     * Process them all.
     * @throws IOException If fails
     */
    private void process() throws IOException {
        final long start = System.currentTimeMillis();
        int total = 0;
        while (true) {
            final Iterator<XML> found = this.find().iterator();
            if (!found.hasNext()) {
                break;
            }
            this.process(found.next());
            ++total;
        }
        if (total > 0) {
            Logger.info(
                this, "Seen %d claims in \"%s\", %[ms]s",
                total, this.project.toString(),
                System.currentTimeMillis() - start
            );
        }
    }

    /**
     * Find the next claim.
     * @return Empty or not
     * @throws IOException If fails
     */
    private Iterable<XML> find() throws IOException {
        final Collection<XML> found = new LinkedList<>();
        try (final Claims claims = new Claims(this.project).lock()) {
            final Iterator<XML> list = claims.iterate().iterator();
            if (list.hasNext()) {
                found.add(list.next());
            }
        }
        return found;
    }

    /**
     * Process it.
     * @param xml The claim
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void process(final XML xml) throws IOException {
        final long start = System.currentTimeMillis();
        final ClaimIn claim = new ClaimIn(xml);
        final int total = this.brigade.process(this.project, xml);
        try (final Claims claims = new Claims(this.project).lock()) {
            claims.remove(claim.number());
        }
        if (total == 0 && claim.hasToken()) {
            throw new IllegalStateException(
                String.format(
                    "Failed to process \"%s\", no stakeholders",
                    claim.type()
                )
            );
        }
        Logger.info(
            this, "Seen \"%s/%d\" at \"%s\" by %d stk, %[ms]s",
            claim.type(), claim.number(), this.project.toString(),
            total,
            System.currentTimeMillis() - start
        );
    }

}
