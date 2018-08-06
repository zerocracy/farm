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
package com.zerocracy.farm;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimIn;
import java.io.IOException;

/**
 * Verbose stakeholder.
 *
 * @since 1.0
 */
public final class StkVerbose implements Stakeholder {

    /**
     * Origin stakeholder.
     */
    private final Stakeholder origin;

    /**
     * Stakeholder name in logs.
     */
    private final String name;

    /**
     * Ctor.
     *
     * @param origin Origin stakeholder
     * @param name Stakeholder name
     */
    public StkVerbose(final Stakeholder origin, final String name) {
        this.origin = origin;
        this.name = name;
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void process(final Project project, final XML claim)
        throws IOException {
        final long start = System.currentTimeMillis();
        this.origin.process(project, claim);
        Logger.info(
            this,
            "Completed ['%s' in '%s' for claim '%s'] in %dms",
            this.name, project.pid(), new ClaimIn(claim).type(),
            System.currentTimeMillis() - start
        );
    }
}
