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

import com.jcabi.xml.Sources;
import com.zerocracy.Item;
import com.zerocracy.Project;
import java.io.IOException;
import java.nio.file.Path;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.cactoos.Input;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputStreamOf;
import org.cactoos.text.TextOf;

/**
 * Ruled rules.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RdSources implements Sources {

    /**
     * Original project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    RdSources(final Project pkt) {
        this.project = pkt;
    }

    @Override
    public Source resolve(final String href, final String base)
        throws TransformerException {
        try (final Item item = this.project.acq(href)) {
            final Path path = item.path();
            final Input input;
            if (path.toFile().length() > 0L) {
                input = new InputOf(item.path());
            } else {
                input = new InputOf("<always-empty/>");
            }
            return new StreamSource(
                new InputStreamOf(new TextOf(input).asString())
            );
        } catch (final IOException ex) {
            throw new TransformerException(ex);
        }
    }

}
