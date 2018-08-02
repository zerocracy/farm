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
package com.zerocracy.farm.reactive;

import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.Stakeholder;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.farm.MismatchException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A stakeholder that doesn't hit the same
 * {@link MismatchException} exception twice.
 *
 * @since 1.0
 */
final class StkSmart implements Stakeholder {

    /**
     * The original one.
     */
    private final Stakeholder origin;

    /**
     * List of places where we already seen problems.
     */
    private final Set<String> places;

    /**
     * Ctor.
     * @param stk Original
     */
    StkSmart(final Stakeholder stk) {
        this.origin = stk;
        this.places = new HashSet<>(0);
    }

    @Override
    public void process(final Project project, final XML xml)
        throws IOException {
        final String place = String.format(
            "%s:%s", project.pid(), new ClaimIn(xml).type()
        );
        synchronized (this.places) {
            if (!this.places.contains(place)) {
                try {
                    this.origin.process(project, xml);
                } catch (final MismatchException ex) {
                    this.places.add(place);
                }
            }
        }
    }
}
