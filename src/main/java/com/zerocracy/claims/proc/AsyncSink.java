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
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseCallable;
import com.zerocracy.shutdown.ShutdownFarm;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Proc;
import org.cactoos.scalar.And;
import org.cactoos.scalar.IoCheckedScalar;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Proc to execute origin proc asynchronously.
 *
 * @since 1.0
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
    private final ShutdownFarm.Hook shutdown;

    /**
     * Counter.
     */
    private final AtomicInteger count;

    /**
     * Claim statuses.
     */
    private final Map<String, Map<String, String>> statuses;

    /**
     * Ctor.
     *
     * @param origin Origin proc
     * @param shutdown Shutdown hook
     * @param statuses Claim statuses
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    public AsyncSink(final Proc<Message> origin,
        final ShutdownFarm.Hook shutdown, final Map<String,
        Map<String, String>> statuses) {
        this.origin = origin;
        this.shutdown = shutdown;
        this.statuses = statuses;
        this.count = new AtomicInteger();
    }

    /**
     * Monitor a queue.
     * @param msg Message to process
     */
    public void execAsync(final Message msg) {
        try {
            this.count.incrementAndGet();
            new ExecFor(msg).value().submit(
                new VerboseCallable<>(
                    () -> {
                        try {
                            this.exec(msg);
                        } catch (final IOException exx) {
                            Logger.error(
                                this,
                                "Failed to process message: %[exception]s",
                                exx
                            );
                        }
                        return null;
                    },
                    true, true
                )
            );
        } catch (final RejectedExecutionException err) {
            this.count.decrementAndGet();
            throw new IllegalStateException("Task was rejected", err);
        }
    }

    /**
     * Amount of tasks processing right now.
     * @return Count
     */
    public int tasks() {
        return this.count.get();
    }

    /**
     * Guts of the sink.
     *
     * @return Xembly dirs
     */
    public Iterable<Directive> guts() {
        final Directives dirs = new Directives();
        dirs.add("stakeholders");
        for (final Map.Entry<String, Map<String, String>> status
            : this.statuses.entrySet()) {
            dirs.add("claim")
                .attr("cid", status.getKey());
            for (final Map.Entry<String, String> stk
                : status.getValue().entrySet()) {
                final String value = stk.getValue();
                if (value.isEmpty()) {
                    continue;
                }
                dirs.add("stakeholder")
                    .add("name").set(stk.getKey()).up()
                    .add("status").set(value).up()
                    .up();
            }
            dirs.up();
        }
        dirs.up();
        return dirs;
    }

    /**
     * Exec a message.
     * @param msg Message
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.PrematureDeclaration")
    private void exec(final Message msg) throws IOException {
        final List<Message> input =
            Collections.singletonList(msg);
        try {
            Logger.info(
                this, "Processing a messages",
                input.size()
            );
            new IoCheckedScalar<>(new And(this.origin, input)).value();
        } finally {
            if (this.count.decrementAndGet() == 0
                && this.shutdown.stopping()) {
                this.shutdown.complete();
                Thread.currentThread().interrupt();
            }
        }
    }
}
