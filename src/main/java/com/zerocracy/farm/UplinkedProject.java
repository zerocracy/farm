/**
 * Copyright (c) 2016 Zerocracy
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

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Project that can fetch files from PMO.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class UplinkedProject implements Project {

    /**
     * Files to fetch from PMO.
     */
    private static final Collection<String> FILES = new HashSet<>(
        Stream.of("catalog.xml", "people.xml").collect(Collectors.toList())
    );

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Is it PMO?
     */
    private final boolean ispmo;

    /**
     * Ctor.
     * @param pkt Project
     * @param frm Farm
     * @param pmo Is it PMO?
     */
    UplinkedProject(final Project pkt, final Farm frm, final boolean pmo) {
        this.origin = pkt;
        this.farm = frm;
        this.ispmo = pmo;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final Item item;
        if (this.ispmo || !UplinkedProject.FILES.contains(file)) {
            item = this.origin.acq(file);
        } else {
            item = new Pmo(this.farm).acq(file);
        }
        return item;
    }

}
