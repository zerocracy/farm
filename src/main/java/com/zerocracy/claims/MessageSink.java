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
package com.zerocracy.claims;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.claims.proc.AsyncSink;
import com.zerocracy.claims.proc.BrigadeProc;
import com.zerocracy.claims.proc.CountingProc;
import com.zerocracy.claims.proc.ExpiryProc;
import com.zerocracy.claims.proc.FootprintProc;
import com.zerocracy.claims.proc.MessageMonitorProc;
import com.zerocracy.claims.proc.ProcGuts;
import com.zerocracy.claims.proc.SentryProc;
import com.zerocracy.farm.guts.Guts;
import com.zerocracy.shutdown.ShutdownHook;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Claims message sink.
 * <p>
 *     This object takes new claims from message queue,
 *     and process them.
 * </p>
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "farm")
public final class MessageSink implements Farm {

    /**
     * Async sink.
     */
    private final AsyncSink asynk;

    /**
     * Origin farm.
     */
    private final Farm farm;

    /**
     * Proc guts.
     */
    private final Iterable<Directive> pguts;

    /**
     * Primary ctr.
     * @param farm Farm
     * @param shutdown Shutdown
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public MessageSink(final Farm farm, final ShutdownHook shutdown) {
        final ProcGuts proc = new ProcGuts(
            new FootprintProc(
                farm,
                new CountingProc(
                    new BrigadeProc(this),
                    new AtomicInteger()
                )
            )
        );
        this.asynk = new AsyncSink(
            new ExpiryProc(
                new MessageMonitorProc(
                    farm, new SentryProc(farm, proc),
                    shutdown
                )
            ),
            shutdown,
            farm
        );
        this.pguts = proc;
        this.farm = farm;
    }

    /**
     * Start sink.
     * @param queue Message queue
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void start(final BlockingQueue<Message> queue) {
        final int processors = Runtime.getRuntime().availableProcessors();
        Logger.info(
            this,
            "Starting message sink with %d async routines",
            processors
        );
        final Thread thread = new Thread(
            () -> {
                int rejects = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (this.asynk.exec(queue.take())) {
                            rejects = 0;
                        } else {
                            ++rejects;
                        }
                        final long wait = (long) Math.min(rejects, Tv.THIRTY);
                        if (wait > 0L) {
                            Logger.info(
                                this,
                                "message rejected %d times, sleeping %d sec",
                                rejects, wait
                            );
                            Thread.sleep(wait * (long) Tv.THOUSAND);
                        }
                    } catch (final InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                        // @checkstyle IllegalCatchCheck (1 line)
                    } catch (final Throwable err) {
                        Logger.error(
                            this, "async sink failed: %[exception]s",
                            err
                        );
                    }
                }
            }
        );
        thread.setDaemon(true);
        thread.setName(this.getClass().getSimpleName());
        thread.start();
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        return new Guts(
            this.farm,
            () -> this.farm.find(xpath),
            () -> new Directives()
                .xpath("/guts")
                .add("farm")
                .attr("id", this.getClass().getSimpleName())
                .append(this.asynk.guts())
                .append(this.pguts)
        ).apply(xpath);
    }

    @Override
    public void close() throws IOException {
        this.farm.close();
    }
}
