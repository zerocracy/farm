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
import com.zerocracy.claims.MsgPriority;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.scalar.UncheckedScalar;

/**
 * Executor service for message.
 *
 * @since 1.0
 */
final class ExecFor implements Scalar<ExecutorService> {

    /**
     * Lazy services.
     */
    private static final UncheckedFunc<MsgPriority, ExecutorService> SERVICES =
        new UncheckedFunc<>(
            new SolidFunc<>(
                prt -> {
                    final ExecutorService exec;
                    final int procs = Runtime.getRuntime()
                        .availableProcessors();
                    if (prt == MsgPriority.HIGH) {
                        exec = Executors.newCachedThreadPool();
                    } else if (prt == MsgPriority.NORMAL) {
                        exec = Executors.newFixedThreadPool(procs);
                    } else {
                        exec = Executors.newFixedThreadPool(procs);
                    }
                    return exec;
                }
            )
        );

    /**
     * Scalar.
     */
    private final UncheckedScalar<ExecutorService> origin;

    /**
     * For message.
     *
     * @param msg Message
     */
    ExecFor(final Message msg) {
        this.origin = new UncheckedScalar<>(
            () -> ExecFor.SERVICES.apply(MsgPriority.from(msg))
        );
    }

    @Override
    public ExecutorService value() {
        return this.origin.value();
    }
}
