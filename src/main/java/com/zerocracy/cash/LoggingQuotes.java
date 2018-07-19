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
package com.zerocracy.cash;

import com.jcabi.log.Logger;
import java.io.IOException;

/**
 * The quotes that logs every single operation.
 *
 * @since 1.0
 */
final class LoggingQuotes implements Quotes {

    /**
     * Origin.
     */
    private final Quotes origin;

    /**
     * Ctor.
     * @param quotes The quotes
     */
    LoggingQuotes(final Quotes quotes) {
        this.origin = quotes;
    }

    @Override
    public double quote(final Currency src, final Currency dest)
        throws IOException {
        final double rate = this.origin.quote(src, dest);
        Logger.info(this, "From %s to %s: %f", src, dest, rate);
        return rate;
    }

}
