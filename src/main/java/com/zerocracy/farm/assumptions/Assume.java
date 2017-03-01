/**
 * Copyright (c) 2016-2017 Zerocracy
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
package com.zerocracy.farm.assumptions;

import com.jcabi.xml.XML;
import com.zerocracy.jstk.Project;

/**
 * Assumptions for stakeholder.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class Assume {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Claim.
     */
    private final XML xml;

    /**
     * Ctor.
     * @param pkt Project
     * @param claim Claim
     */
    public Assume(final Project pkt, final XML claim) {
        this.project = pkt;
        this.xml = claim;
    }

    /**
     * Roles.
     * @param type Type to apply
     */
    public AeType type(final String type) {
        return new AeType(this.xml, type);
    }

    /**
     * Roles.
     * @param roles List of roles
     */
    public AeRoles roles(final String... roles) {
        return new AeRoles(
            this.project, this.xml, roles
        );
    }

}
