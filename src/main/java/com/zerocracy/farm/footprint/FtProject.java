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
package com.zerocracy.farm.footprint;

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.EqualsAndHashCode;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;

/**
 * Footprint project.
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class FtProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Ctor.
     * @param pkt Project
     * @param frm Farm
     */
    FtProject(final Project pkt, final Farm frm) {
        this.origin = pkt;
        this.farm = frm;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        Item item = this.origin.acq(file);
        if ("claims.xml".equals(file)) {
            final Path temp = Files.createTempFile("footprint", ".xml");
            final Path before = item.path();
            if (before.toFile().exists()) {
                new LengthOf(new TeeInput(item.path(), temp)).intValue();
            }
            item = new FtItem(this, item, this.farm, temp);
        }
        return item;
    }

}
