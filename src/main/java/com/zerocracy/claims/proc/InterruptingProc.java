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
import org.cactoos.Proc;

/**
 * Proc which handle interrupted exceptions.
 *
 * @since 1.0
 */
public final class InterruptingProc implements Proc<Message> {

    /**
     * Origin proc.
     */
    private final Proc<Message> proc;

    /**
     * Ctor.
     * @param proc Origin
     */
    public InterruptingProc(final Proc<Message> proc) {
        this.proc = proc;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void exec(final Message input) throws Exception {
        try {
            this.proc.exec(input);
            // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable err) {
            if (InterruptingProc.causedByInterrupted(err)) {
                final Thread thread = Thread.currentThread();
                thread.interrupt();
                Logger.info(
                    this,
                    "The thread %s was interrupted",
                    thread.getName()
                );
            } else {
                throw err;
            }
        }
    }

    /**
     * Check if exception was caused by interrupted exception.
     * @param thr Throwable
     * @return TRUE if yes
     */
    private static boolean causedByInterrupted(final Throwable thr) {
        return thr.getClass().equals(InterruptedException.class)
            || thr.getCause() != null
            && InterruptingProc.causedByInterrupted(thr.getCause());
    }
}
