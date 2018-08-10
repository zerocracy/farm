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

import com.jcabi.xml.XML;
import java.io.IOException;

/**
 * Stakeholder which collects execution metrics.
 *
 * @since 1.0
 * @todo #1172:30min Add performance metrics: add measurement of time taken
 *  to execute Measured stakeholder. Time taken must be saved to mongodb
 *  instance on database "metrics", collection "time_taken". Fields to be saved:
 *  "stakeholder" with stakeholder class qualified name, "claim_type" with
 *  claim type, "date_time" with datetime of execution, "time_taken" with time
 *  taken to stakeholder process claim. After this decorate StkRuntime so it
 *  could really collect the metrics and fix test in MeasuredText
 *  .collectTimeTakenToExecute if needed.
 * @todo #1172:30min Add performance metrics: add measurement of other
 *  performance metrics (track S3 artifacts download, xml document
 *  modifications, footprint access) and save the to mongodb under database
 *  "metrics".
 */
public final class Measured implements Stakeholder {

    /**
     * Mongo collection name.
     */
    public static final String COLLECTION = "metrics";

    /**
     * Mongo time taken metric name.
     */
    public static final String TIME_TAKEN = "time_taken";

    /**
     * Stakeholder to be measured.
     */
    private final Stakeholder stk;

    /**
     * Ctor.
     * @param stk Stakeholder to be measured
     */
    public Measured(final Stakeholder stk) {
        this.stk = stk;
    }

    @Override
    public void process(final Project project, final XML claim)
        throws IOException {
        this.stk.process(project, claim);
        throw new IllegalStateException("measured stakeholder not implemented");
    }
}
