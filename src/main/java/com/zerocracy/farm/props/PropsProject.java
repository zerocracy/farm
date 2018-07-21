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
package com.zerocracy.farm.props;

import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.nio.file.Files;
import lombok.EqualsAndHashCode;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Props project.
 *
 * <p>A project which acquires its {@code _props.xml} file and also
 * adds some post processing directives to it.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "origin")
final class PropsProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Post processing.
     */
    private final Iterable<Directive> post;

    /**
     * Ctor.
     * @param pkt Project
     */
    PropsProject(final Project pkt) {
        this(pkt, new Directives());
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param dirs Post processing dirs
     */
    PropsProject(final Project pkt, final Iterable<Directive> dirs) {
        this.origin = pkt;
        this.post = dirs;
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final Item item;
        if ("_props.xml".equals(file)) {
            item = new PropsItem(
                Files.createTempFile("props", ".xml"), this.post
            );
        } else {
            item = this.origin.acq(file);
        }
        return item;
    }

}
