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
import java.io.IOException;
import java.time.Instant;

/**
 * Claims.
 *
 * @since 1.0
 */
public interface Claims {

    /**
     * Submit new claim.
     *
     * @param claim Claim to submit
     * @throws IOException If fails
     */
    void submit(XML claim) throws IOException;

    /**
     * Submit new claim with a time to live.
     *
     * @param claim Claim to submit
     * @param expires When this claim expires
     * @throws IOException If fails
     */
    default void submit(final XML claim, final Instant expires)
        throws IOException {
        this.submit(claim);
    }
}
