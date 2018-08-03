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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseCallable;
import com.jcabi.log.VerboseThreads;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.cactoos.Proc;

/**
 * Proc to execute origin proc asynchronously.
 *
 * @since 1.0
 */
public final class AsyncProc implements Proc<Message> {

    /**
     * Executor.
     */
    private final ExecutorService service;

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Ctor.
     *
     * @param origin Origin proc
     */
    public AsyncProc(final Proc<Message> origin) {
        this(Runtime.getRuntime().availableProcessors(), origin);
    }

    /**
     * Ctor.
     *
     * @param threads Threads
     * @param origin Origin proc
     */
    public AsyncProc(final int threads, final Proc<Message> origin) {
        this.service = Executors.newFixedThreadPool(
            threads, new VerboseThreads(AsyncProc.class)
        );
        this.origin = origin;
    }

    @Override
    public void exec(final Message input) {
        this.service.submit(
            new VerboseCallable<>(
                () -> {
                    Logger.info(
                        this, "Processing message %s",
                        input.getMessageId()
                    );
                    this.origin.exec(input);
                    return null;
                },
                true, true
            )
        );
        Logger.info(
            this, "Submitted message %s",
            input.getMessageId()
        );
    }
}
