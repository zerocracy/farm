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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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
     * Stakeholder status.
     */
    private final Map<String, Map<String, String>> statuses;

    /**
     * Ctor.
     *  @param origin Origin stakeholder
     * @param name Stakeholder name
     * @param statuses Stakeholder statuses
     */
    public StkVerbose(final Stakeholder origin, final String name,
        final Map<String, Map<String, String>> statuses) {
        this.origin = origin;
        this.name = name;
        this.statuses = statuses;
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void process(final Project project, final XML xml)
        throws IOException {
        final long start = System.currentTimeMillis();
        final ClaimIn claim = new ClaimIn(xml);
        if (!this.statuses.containsKey(claim.cid())) {
            synchronized (this.statuses) {
                if (!this.statuses.containsKey(claim.cid())) {
                    this.statuses.put(claim.cid(), new HashMap<>(1));
                }
            }
        }
        final Map<String, String> status = this.statuses.get(claim.cid());
        status.put(
            this.name,
            String.format(
                "%s/'%s'/%s (%s)",
                claim.cid(), claim.type(), project.pid(), Instant.now()
            )
        );
        try {
            this.origin.process(project, xml);
        } finally {
            status.remove(this.name);
        }
        Logger.info(
            this,
            "Completed ['%s' in '%s' for claim '%s'] in %dms",
            this.name, project.pid(), claim.type(),
            System.currentTimeMillis() - start
        );
    }
}
