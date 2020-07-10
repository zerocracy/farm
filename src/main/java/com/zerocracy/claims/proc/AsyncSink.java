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

import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.claims.ClaimsQueueUrl;
import com.zerocracy.claims.MsgPriority;
import com.zerocracy.entry.ExtSqs;
import com.zerocracy.shutdown.ShutdownHook;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.cactoos.Proc;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Proc to execute origin proc asynchronously.
 *
 * @since 1.0
 * @todo #2165:30min - Lets introduce new `Farm`, `Project`
 *  and `Item` implementation which should check that `project.acq()`
 *  and `item.close()` are called on proper project thread
 *  (see this class for thread name pattern), then
 *  replace `SyncProject` with new implementation. It will trigger some
 *  design issues (like some stakeholders may access resources of different
 *  projects), so it should be solved before.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class AsyncSink {

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Shutdown hook.
     */
    private final ShutdownHook shutdown;

    /**
     * Queue per project map.
     */
    private final ConcurrentMap<String, ProjectQueue> queues;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     *
     * @param origin Origin proc
     * @param shutdown Shutdown hook
     * @param farm Farm
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    public AsyncSink(final Proc<Message> origin,
        final ShutdownHook shutdown, final Farm farm) {
        this.origin = origin;
        this.shutdown = shutdown;
        this.farm = farm;
        this.queues = new ConcurrentHashMap<>(Tv.FIFTY);
    }

    /**
     * Process a message.
     * @param msg Message to process
     * @return True if was executed
     * @throws IOException If fails
     */
    public boolean exec(final Message msg) throws IOException {
        if (this.shutdown.stopping()) {
            Logger.info(this, "Shutdown requested, stopping all queues");
            this.queues.values().forEach(ProjectQueue::stop);
            this.queues.clear();
            throw new IOException("Shutting down");
        }
        final String pid = msg.getMessageAttributes().get("project")
            .getStringValue();
        final ProjectQueue queue = this.queues.computeIfAbsent(
            pid, this::startedQueue
        );
        final ProjectQueue repaired = queue.repair();
        final boolean process = repaired.size() < Tv.EIGHT
            || MsgPriority.from(msg).value() > MsgPriority.LOW.value();
        if (process) {
            repaired.push(msg);
        } else {
            Logger.info(
                this, "project queue %s is full, releasing message",
                repaired.toString()
            );
            new IoCheckedScalar<>(new ExtSqs(this.farm)).value()
                .changeMessageVisibility(
                    new ChangeMessageVisibilityRequest()
                        .withQueueUrl(new ClaimsQueueUrl(this.farm).asString())
                        .withVisibilityTimeout(0)
                        .withReceiptHandle(msg.getReceiptHandle())
                );
            Logger.info(
                this, "message %s was released",
                msg.getMessageId()
            );
        }
        return process;
    }

    /**
     * Guts of the sink.
     *
     * @return Xembly dirs
     * @throws IOException If fails
     */
    public Iterable<Directive> guts() throws IOException {
        return new Directives()
            .add("queues")
            .append(
                new IoCheckedScalar<>(
                    new Reduced<>(
                        new Directives(),
                        (dirs, queue) -> dirs.append(queue.stats()),
                        this.queues.values()
                    )
                ).value()
            ).up();
    }

    /**
     * Create new project queue and start it.
     * @param pid Project id
     * @return Queue
     */
    private ProjectQueue startedQueue(final String pid) {
        final ProjectQueue queue = new ProjectQueue(pid, this.origin);
        queue.start();
        return queue;
    }
}
