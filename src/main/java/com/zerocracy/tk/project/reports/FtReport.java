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
package com.zerocracy.tk.project.reports;

import com.zerocracy.Project;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.bson.conversions.Bson;

/**
 * Footprint report.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public interface FtReport {

    /**
     * Create request for MongoDB.
     * @param project The project
     * @param start The start date
     * @param end The end date
     * @return BSON for MongoDB aggregate
     * @throws IOException If fails
     */
    List<? extends Bson> bson(Project project,
        Instant start, Instant end) throws IOException;

    /**
     * Its title, in HTML.
     * @return Title of the report.
     * @throws IOException If fails
     */
    String title() throws IOException;

}
