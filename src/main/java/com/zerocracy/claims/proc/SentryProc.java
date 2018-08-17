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
import com.zerocracy.Farm;
import com.zerocracy.sentry.SafeSentry;
import org.cactoos.Proc;

/**
 * Posts failures to Sentry.
 *
 * @since 1.0
 */
public final class SentryProc implements Proc<Message> {

    /**
     * Sentry.
     */
    private final SafeSentry sentry;

    /**
     * Origin proc.
     */
    private final Proc<Message> origin;

    /**
     * Ctor.
     *
     * @param farm Farm
     * @param origin Origin proc
     */
    public SentryProc(final Farm farm, final Proc<Message> origin) {
        this(new SafeSentry(farm), origin);
    }

    /**
     * Ctor.
     *
     * @param sentry Sentry
     * @param origin Origin proc
     */
    public SentryProc(final SafeSentry sentry, final Proc<Message> origin) {
        this.sentry = sentry;
        this.origin = origin;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void exec(final Message input) {
        try {
            this.origin.exec(input);
            // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable err) {
            this.sentry.capture(err);
        }
    }
}
