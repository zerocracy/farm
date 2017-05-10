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
import com.zerocracy.farm.MismatchException;
import com.zerocracy.pm.ClaimIn;
import java.util.Locale;

/**
 * Assume type.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class AeType {

    /**
     * Claim.
     */
    private final XML xml;

    /**
     * Type.
     */
    private final String type;

    /**
     * Ctor.
     * @param claim Claim
     * @param tpe Type
     */
    public AeType(final XML claim, final String tpe) {
        this.xml = claim;
        this.type = tpe;
    }

    /**
     * Equals.
     * @throws MismatchException If doesn't match
     */
    public void exact() throws MismatchException {
        final String input = new ClaimIn(this.xml)
            .type().toLowerCase(Locale.ENGLISH);
        final String expected = this.type.toLowerCase(Locale.ENGLISH);
        if (!input.equals(expected)) {
            throw new MismatchException(
                String.format(
                    "Type \"%s\" is not mine, I'm expecting \"%s\"",
                    input, expected
                )
            );
        }
    }

}
