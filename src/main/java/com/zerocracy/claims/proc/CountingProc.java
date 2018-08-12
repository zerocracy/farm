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
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Proc;

/**
 * Count currently processed messages.
 *
 * @since 1.0
 */
public final class CountingProc implements Proc<Message> {

    /**
     * Decorated proc.
     */
    private final Proc<Message> proc;

    /**
     * Counter.
     */
    private final AtomicInteger count;

    /**
     * Ctor.
     *
     * @param proc Proc being decorated
     * @param count Counter of executing procedures
     */
    public CountingProc(final Proc<Message> proc, final AtomicInteger count) {
        this.proc = proc;
        this.count = count;
    }

    @Override
    public void exec(final Message input) throws Exception {
        try {
            this.count.incrementAndGet();
            this.proc.exec(input);
        } finally {
            this.count.decrementAndGet();
        }
    }
}
