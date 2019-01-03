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
package com.zerocracy.farm;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimIn;
import java.io.IOException;
import java.time.Duration;

/**
 * Proc to warn about long claim processing.
 *
 * @since 1.0
 */
public final class StkTimed implements Stakeholder {

    /**
     * Origin stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Stakeholder name.
     */
    private final String name;

    /**
     * Threshold duration.
     */
    private final Duration threshold;

    /**
     * Ctor.
     *  @param origin Origin stakeholder
     * @param name Stakeholder name
     * @param threshold Threshold duration to warn
     */
    public StkTimed(final Stakeholder origin, final String name,
        final Duration threshold) {
        this.origin = origin;
        this.threshold = threshold;
        this.name = name;
    }

    @Override
    public void process(final Project project, final XML claim)
        throws IOException {
        final long start = System.nanoTime();
        try {
            this.origin.process(project, claim);
        } finally {
            final long time = System.nanoTime() - start;
            if (time > this.threshold.toNanos()) {
                Logger.warn(
                    this,
                    // @checkstyle LineLengthCheck (1 line)
                    "Stakeholder '%s' in project '%s' was completed in %[nano]s for claim '%s'",
                    this.name, project.pid(), time, new ClaimIn(claim).type()
                );
            }
        }
    }
}
