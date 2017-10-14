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
package com.zerocracy.farm.sync;

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import com.zerocracy.jstk.Project;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.cactoos.func.RunnableOf;

/**
 * Terminator.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class Terminator {

    /**
     * Threshold of locking, in seconds.
     */
    private final long threshold;

    /**
     * Terminator of long running threads.
     */
    private final ExecutorService service;

    /**
     * Map of statuses per project.
     */
    private final Map<Project, Boolean> killers;

    /**
     * Ctor.
     * @param sec Seconds to give to each thread
     */
    Terminator(final long sec) {
        this.threshold = sec;
        this.service = Executors.newCachedThreadPool(new VerboseThreads());
        this.killers = new ConcurrentHashMap<>(0);
    }

    /**
     * Submit new one.
     * @param project The project
     * @param file The file
     * @param lock The lock
     */
    public void submit(final Project project, final String file,
        final ReentrantLock lock) {
        synchronized (this.killers) {
            if (!this.killers.containsKey(project)) {
                this.killers.put(project, true);
                this.service.submit(this.killer(project, file, lock));
            }
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
        final ReentrantLock lock) {
        final Thread thread = Thread.currentThread();
        final Exception location = new IllegalStateException("Here!");
        return new RunnableOf<Object>(
            input -> {
                if (!lock.tryLock(this.threshold, TimeUnit.SECONDS)) {
                    thread.interrupt();
                    Logger.warn(
                        this,
                        // @checkstyle LineLength (1 line)
                        "Thread %d/%s interrupted because of too long hold of \"%s\" in %s, holdCount=%d, queueLength=%d, hasQueuedThreads=%b, isHeldByCurrentThread=%b: %[exception]s",
                        thread.getId(), thread.getName(),
                        file, project,
                        lock.getHoldCount(),
                        lock.getQueueLength(),
                        lock.hasQueuedThreads(),
                        lock.isHeldByCurrentThread(),
                        location
                    );
                }
                lock.unlock();
                this.killers.remove(project);
            }
        );
    }
}
