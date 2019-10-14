/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.claims.MsgPriority;
import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.EqualsAndHashCode;
import org.cactoos.Proc;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Project queue of claim messages ordered by priority.
 *
 * @since 1.0
 */
@EqualsAndHashCode
public final class ProjectQueue {

    /**
     * Queue order comparator.
     */
    private static final Comparator<Message> CMP =
        Comparator.comparing(MsgPriority::from).reversed();

    /**
     * Message queue.
     */
    private final BlockingQueue<Message> msgs;

    /**
     * Project id.
     */
    private final String pid;

    /**
     * Queue thread.
     */
    private final Thread thread;

    /**
     * Message proc.
     */
    private final Proc<Message> proc;

    /**
     * Ctor.
     * @param pid Project id
     * @param proc Stakeholders
     */
    public ProjectQueue(final String pid, final Proc<Message> proc) {
        this(
            new PriorityBlockingQueue<>(Tv.HUNDRED, ProjectQueue.CMP),
            pid, proc
        );
    }

    /**
     * Primary ctor.
     * @param msgs Message queue
     * @param pid Project id
     * @param proc Message proc
     */
    ProjectQueue(final BlockingQueue<Message> msgs, final String pid,
        final Proc<Message> proc) {
        this.msgs = msgs;
        this.pid = pid;
        this.thread = ProjectQueue.newThread(pid, this::run);
        this.proc = proc;
    }

    /**
     * Push message to the queue.
     * @param msg Message to push
     */
    public void push(final Message msg) {
        if (this.thread.isInterrupted()) {
            throw new IllegalStateException(
                String.format(
                    "Thread %s was interrupted, state=%s",
                    this.thread.getName(), this.thread.getState()
                )
            );
        }
        try {
            this.msgs.put(msg);
            Logger.info(
                this, "Pushed message (queue_size=%d, pri=%s): %s",
                this.msgs.size(), MsgPriority.from(msg), msg.getMessageId()
            );
        } catch (final InterruptedException err) {
            Thread.currentThread().interrupt();
            Logger.warn(
                this, "push for %s interrupted: %[exception]s",
                this.pid, err
            );
        }
    }

    /**
     * Start thread queue.
     */
    public void start() {
        Logger.info(this, "Starting queue: %s", this.pid);
        this.thread.start();
        Logger.info(
            this, "Queue %s started: %s", this.pid, this.thread.getState()
        );
    }

    /**
     * Stop queue thread.
     */
    public void stop() {
        Logger.info(this, "Stopping queue %s", this.pid);
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (final InterruptedException err) {
            Logger.info(
                this,
                "Queue thread %s was interrupted: %[exception]s",
                this.thread.getName(), err
            );
        }
        Logger.info(this, "Queue stopped %s", this.pid);
    }

    /**
     * Queue size.
     * @return Size
     */
    public int size() {
        return this.msgs.size();
    }

    /**
     * Project queu details in Xembly format.
     * @return Xembly directives
     * @throws IOException If fails
     */
    public Iterable<Directive> stats() throws IOException {
        return new Directives()
            .add("queue").attr("pid", this.pid)
            .add("thread")
            .add("name").set(this.thread.getName()).up()
            .add("state").set(this.thread.getState()).up()
            .up()
            .add("size").set(this.size()).up()
            .add("items")
            .append(
                new IoCheckedScalar<>(
                    new Reduced<>(
                        new Directives(),
                        (bld, msg) -> bld.add("item")
                            .attr("id", msg.getMessageId())
                            .add("body").set(msg.getBody()).up()
                            .up(),
                        this.msgs
                    )
                ).value()
            ).up().up();
    }

    /**
     * Repair current queue if broken.
     * @return New queue if broken or current of OK
     * @todo #2165:30min Some threads are getting terminated by someone
     *  from outside. More often PQ-PMO thread is interrupted.
     *  Most probably `Terminator` is the problem here. Let's fix this
     *  issue and remove this dirty hack from this object.
     */
    public ProjectQueue repair() {
        final ProjectQueue res;
        if (this.thread.getState() == Thread.State.TERMINATED) {
            Logger.warn(
                this,
                "Thread terminated, repairing queue: %s", this.pid
            );
            res = new ProjectQueue(this.msgs, this.pid, this.proc);
            res.start();
        } else {
            res = this;
        }
        return res;
    }

    @Override
    public String toString() {
        return String.format("ProjectQueue(%s)", this.pid);
    }

    /**
     * Runnable job.
     */
    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "OverlyBroadCatchBlock"})
    private void run() {
        final Thread thr = Thread.currentThread();
        while (!thr.isInterrupted()) {
            final Message msg;
            try {
                msg = this.msgs.take();
            } catch (final InterruptedException err) {
                thr.interrupt();
                Logger.info(
                    this,
                    "Project queue was interrupted: %[exception]s", err
                );
                break;
            }
            Logger.info(
                this,
                "Polled message (queue_size=%d, pri=%s): %s",
                this.msgs.size(), MsgPriority.from(msg), msg.getMessageId()
            );
            try {
                this.proc.exec(msg);
            } catch (final InterruptedException iex) {
                thr.interrupt();
                Logger.warn(
                    this,
                    "Proc interrupted: %[exception]s", iex
                );
                break;
                // @checkstyle IllegalCatch (1 line)
            } catch (final Throwable err) {
                Logger.error(
                    this,
                    "Proc failed for message %s: %[exception]s",
                    msg, err
                );
            }
        }
        Logger.warn(
            this, "Queue exited: interrupted?=%b state=%s",
            thr.isInterrupted(), thr.getState()
        );
    }

    /**
     * New thread for project.
     * @param pid Project id
     * @param worker Thread worker runnable
     * @return Thread instance
     */
    private static Thread newThread(final String pid, final Runnable worker) {
        final Thread thr = new Thread(worker, String.format("PQ-%s", pid));
        thr.setDaemon(false);
        return thr;
    }
}
