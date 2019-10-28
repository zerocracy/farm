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

import com.jcabi.github.Coordinates;
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import com.zerocracy.entry.ExtGithub;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * GitHub health check.
 *
 * @since 1.0
 */
final class GhCheck implements HealthCheck {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Output.
     */
    private final AtomicReference<String> output;

    /**
     * Ctor.
     * @param farm Farm
     */
    GhCheck(final Farm farm) {
        this.farm = farm;
        this.output = new AtomicReference<>("none");
    }

    @Override
    public void run() {
        try {
            new ExtGithub(this.farm).value().repos()
                .get(new Coordinates.Simple("zerocracy/farm"))
                .json();
            this.output.set("OK");
        } catch (final IOException err) {
            Logger.error(
                this,
                "Failed to find farm repo: %[exception]s", err
            );
            this.output.set(err.getMessage());
        }
    }

    @Override
    public String status() {
        return this.output.get();
    }
}
