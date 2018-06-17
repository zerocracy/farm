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
package com.zerocracy.farm.sync;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.Project;
import com.zerocracy.ShutUp;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.cactoos.Scalar;
import org.cactoos.func.RunnableOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Terminator.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class Terminator implements Closeable, Scalar<Iterable<Directive>> {

    /**
     * Threshold of locking, in milliseconds.
     */
    private final long threshold;

    /**
     * Terminator of long running threads.
     */
    private final ExecutorService service;

    /**
     * Map of statuses per project.
     */
    private final Map<Project, String> killers;

    /**
     * Ctor.
     * @param msec Seconds to give to each thread
     */
    Terminator(final long msec) {
        this.threshold = msec;
        this.service = Executors.newCachedThreadPool(
            new VerboseThreads(
                String.format("Terminator-%d-", msec)
            )
        );
        this.killers = new ConcurrentHashMap<>(0);
    }

    @Override
    public void close() {
        new ShutUp(this.service).close();
    }

    @Override
    public Iterable<Directive> value() throws Exception {
        return new Directives().add("terminator").append(
            new Joined<Directive>(
                new Mapped<>(
                    pkt -> new Directives().add("killer")
                        .attr("pid", pkt.getKey().pid())
                        .set(pkt.getValue()).up(),
                    this.killers.entrySet()
                )
            )
        ).up();
    }
    /**
     * Submit new one.
     * @param project The project
     * @param file The file
     * @param lock The lock
     */
    public void submit(final Project project, final String file,
        final Lock lock) {
        synchronized (this.killers) {
            this.killers.putIfAbsent(project, file);
            this.service.submit(
                new VerboseRunnable(
                    this.killer(project, file, lock),
                    true, true
                )
            );
        }
    }

    /**
     * Killer runnable.
     * @param project The project
     * @param file The file
     * @param lock The lock
     * @return The runnable
     */
    private Runnable killer(final Project project, final String file,
        final Lock lock) {
        final Thread thread = Thread.currentThread();
        final Exception location = new IllegalStateException("Here!");
        return new RunnableOf<Object>(
            input -> {
                if (lock.tryLock(this.threshold, TimeUnit.MILLISECONDS)) {
                    lock.unlock();
                } else {
                    Logger.warn(
                        this,
                        // @checkstyle LineLength (1 line)
                        "Thread %d/%s interrupted because of too long hold of \"%s\" in %s (over %d msec), %s: %[exception]s",
                        thread.getId(), thread.getName(),
                        file, project.pid(), this.threshold, lock, location
                    );
                    thread.interrupt();
                    this.submit(project, file, lock);
                }
                this.killers.remove(project);
            }
        );
    }

}
