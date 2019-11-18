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
package com.zerocracy.health;

import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stakeholder brigade health check.
 *
 * @since 1.0
 */
final class StkCheck implements HealthCheck {

    /**
     * Last check run.
     */
    private final AtomicReference<Instant> last;

    /**
     * Ctor.
     */
    StkCheck() {
        this.last = new AtomicReference<>(Instant.now());
    }

    @Override
    public void run() throws IOException {
        this.last.set(Instant.now());
    }

    @Override
    public String status() throws IOException {
        final String stt;
        if (Duration.between(this.last.get(), Instant.now()).toHours() > 1L) {
            stt = "STOPPED";
            // @checkstyle LineLengthCheck (1 line)
        } else if (Duration.between(this.last.get(), Instant.now()).toMinutes() > (long) Tv.FIFTEEN) {
            stt = "SLOW";
        } else {
            stt = "OK";
        }
        return stt;
    }
}
