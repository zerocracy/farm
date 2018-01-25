/**
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
package com.zerocracy.err;

import com.zerocracy.SoftException;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import java.io.IOException;
import org.cactoos.Scalar;

/**
 * Reaction with fallback.
 *
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.21
 */
public final class FbReaction {
    /**
     * Sentry client.
     */
    private final SentryClient sentry;
    /**
     * Rethrow.
     */
    private final boolean rthr;

    /**
     * Ctor.
     */
    public FbReaction() {
        this(true);
    }

    /**
     * Ctor.
     * @param rethrow Rethrow fatal errors
     */
    public FbReaction(final boolean rethrow) {
        this(Sentry.getStoredClient(), rethrow);
    }

    /**
     * Ctor.
     * @param sentry Sentry client
     * @param rethrow Rethrow fatal errors
     */
    public FbReaction(final SentryClient sentry, final boolean rethrow) {
        this.sentry = sentry;
        this.rthr = rethrow;
    }

    /**
     * React with fallback.
     * @param reaction Reaction
     * @param fallback Fallback
     * @return Reaction result
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public boolean react(
        final Scalar<Boolean> reaction,
        final ReFallback fallback
    ) throws IOException {
        boolean result = false;
        try {
            result = reaction.value();
        } catch (final SoftException err) {
            fallback.process(err);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception err) {
            fallback.process(err);
            this.sentry.sendException(err);
            if (this.rthr) {
                throw new IOException(err);
            }
        }
        return result;
    }
}
