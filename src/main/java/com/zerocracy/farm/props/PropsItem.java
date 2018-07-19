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

import com.jcabi.xml.XMLDocument;
import com.zerocracy.Item;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.EqualsAndHashCode;
import org.cactoos.io.LengthOf;
import org.cactoos.io.ResourceOf;
import org.cactoos.io.TeeInput;
import org.cactoos.text.TextOf;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Props item.
 *
 * <p>This Item represents the {@code _props.xml} file in a PMO project.</p>
 *
 * @since 1.0
 */
@EqualsAndHashCode(of = "temp")
final class PropsItem implements Item {

    /**
     * Temp file.
     */
    private final Path temp;

    /**
     * Post processing.
     */
    private final Iterable<Directive> post;

    /**
     * Ctor.
     * @param tmp Temp file
     */
    PropsItem(final Path tmp) {
        this(tmp, new Directives());
    }

    /**
     * Ctor.
     * @param tmp Temp file
     * @param dirs Post processing dirs
     */
    PropsItem(final Path tmp, final Iterable<Directive> dirs) {
        this.temp = tmp;
        this.post = dirs;
    }

    @Override
    public String toString() {
        return "_props.xml";
    }

    @Override
    public Path path() throws IOException {
        final Directives dirs = new Directives();
        if (this.getClass().getResource("/org/junit/Test.class") != null) {
            dirs.xpath("/props").add("testing").set("yes");
        }
        dirs.append(this.post);
        new LengthOf(
            new TeeInput(
                new XMLDocument(
                    new Xembler(dirs).applyQuietly(
                        new XMLDocument(
                            new TextOf(
                                new ResourceOf("com/zerocracy/_props.xml")
                            ).asString()
                        ).node()
                    )
                ).toString(),
                this.temp
            )
        ).intValue();
        return this.temp;
    }

    @Override
    public void close() throws IOException {
        Files.delete(this.temp);
    }

}
