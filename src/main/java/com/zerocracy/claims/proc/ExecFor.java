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
import com.jcabi.log.VerboseThreads;
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
 * @checkstyle LineLengthCheck
 * @todo #2150:30min - Lets introduce new `Farm`, `Project` and `Item` implementation
 *  which should check that `project.acq()` and `item.close()` are called on
 *  proper project thread (see this class for thread name pattern), then
 *  replace `SyncProject` with new implementation. It will trigger some
 *  design issues (like some stakeholders may access resources of different
 *  projects), so it should be solved before.
 */
final class ExecFor implements Scalar<ExecutorService> {

    /**
     * Lazy services.
     */
    private static final UncheckedFunc<String, ExecutorService> SERVICES =
        new UncheckedFunc<>(
            new SolidFunc<>(
                pid -> Executors.newSingleThreadExecutor(
                    new VerboseThreads(
                        String.format("FARM-PROJ-%s", pid),
                        false, Thread.NORM_PRIORITY
                    )
                )
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
            () -> ExecFor.SERVICES.apply(
                msg.getMessageAttributes()
                    .get("project")
                    .getStringValue()
            )
        );
    }

    @Override
    public ExecutorService value() {
        return this.origin.value();
    }
}
