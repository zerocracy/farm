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
package com.zerocracy.claims;

import com.jcabi.xml.XML;
import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;

/**
 * Batch processing of {@link Claims}.
 *
 * @since 1.0
 * @todo #1545:30min Implement BatchClaims for sending claims in batch.
 *  Assure that maximum batch size is 256KB for each batch and after
 *  implementation remove expected exception from tests in BatchClaimsTest.
 */
public final class BatchClaims implements Claims, Closeable {

    /**
     * Maximum batch size (in KB).
     */
    private final int max;

    /**
     * Default ctor.
     */
    BatchClaims() {
        // @checkstyle MagicNumberCheck (1 line)
        this(256);
    }

    /**
     * Ctor.
     * @param max Maximum batch size, in KB
     */
    BatchClaims(final int max) {
        this.max = max;
    }

    @Override
    public void submit(final XML claim) throws IOException {
        throw new UnsupportedOperationException("submit(XML) not implemented");
    }

    @Override
    public void submit(final XML claim, final Instant expires) {
        throw new UnsupportedOperationException(
            "submit(XML, Instant) not implemented"
        );
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("close() not implemented");
    }

    /**
     * Return the maximum batch size (in KB) allowed.
     * @return Maximum batch size (in KB)
     */
    public int maximum() {
        return this.max;
    }

}
