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
package com.zerocracy;

import com.jcabi.log.Logger;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;

/**
 * Safe wrapper for {@link io.sentry.Sentry}.
 *
 * @since 1.0
 */
public final class SafeSentry {
    /**
     * Encapsulated Sentry client.
     */
    private final SentryClient client;

    /**
     * Ctor.
     */
    public SafeSentry() {
        this(SentryClientFactory.sentryClient());
    }

    /**
     * Ctor.
     * @param client Sentry client
     */
    SafeSentry(final SentryClient client) {
        this.client = client;
    }

    /**
     * Capture exception.
     * @param error Source throwable
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void capture(final Throwable error) {
        try {
            this.client.sendException(error);
            // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Logger.error(
                SafeSentry.class, "Sentry threw an error: %[exception]s", ex
            );
        }
    }
}
