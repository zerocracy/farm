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
package com.zerocracy.farm.ruled;

import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import lombok.EqualsAndHashCode;

/**
 * Ruled project.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
final class RdProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Ctor.
     * @param pkt Project
     */
    RdProject(final Project pkt) {
        this.origin = pkt;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        Item item = this.origin.acq(file);
        if (!"claims.xml".equals(file)
            && file.charAt(0) != '_'
            && file.endsWith(".xml")) {
            item = new RdItem(this, item, file);
        }
        return item;
    }

}
