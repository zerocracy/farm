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

import com.zerocracy.Farm;
import java.io.IOException;
import java.util.Map;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;

/**
 * Health checks.
 *
 * @since 1.0
 */
public final class HealthChecks {

    /**
     * Checks.
     */
    private final Map<String, HealthCheck> checks;

    /**
     * Ctor.
     * @param farm Farm
     */
    private HealthChecks(final Farm farm) {
        this.checks = new MapOf<String, HealthCheck>(
            new MapEntry<>("aws-s3", new S3Check(farm)),
            new MapEntry<>("aws-sqs", new SqsCheck(farm)),
            new MapEntry<>("github", new GhCheck(farm)),
            new MapEntry<>("brigade", new StkCheck())
        );
    }

    /**
     * Run all checks.
     * @throws IOException If fails
     */
    public void run() throws IOException {
        for (final HealthCheck check : this.checks.values()) {
            check.run();
        }
    }

    /**
     * Check status.
     * @param name Check name
     * @return Status
     * @throws IOException If fails
     */
    public String status(final String name) throws IOException {
        return this.checks.get(name).status();
    }

    /**
     * Ext checks.
     */
    public static final class Ext implements Scalar<HealthChecks> {

        /**
         * Factory scalar.
         */
        private static final Func<Farm, HealthChecks> FACTORY =
            new SolidFunc<>(HealthChecks::new);

        /**
         * Farm.
         */
        private final Farm farm;

        /**
         * Ctor.
         * @param farm Farm
         */
        public Ext(final Farm farm) {
            this.farm = farm;
        }

        @Override
        public HealthChecks value() throws Exception {
            return HealthChecks.Ext.FACTORY.apply(this.farm);
        }
    }
}
