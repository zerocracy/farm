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

import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.net.URI;
import org.cactoos.Input;
import org.cactoos.cache.SoftFunc;
import org.cactoos.func.SyncFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.InputOf;
import org.cactoos.io.InputWithFallback;
import org.cactoos.io.StickyInput;
import org.cactoos.io.SyncInput;
import org.cactoos.text.TextOf;

/**
 * Ruled rules.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class RdIndex {

    /**
     * Cache of documents.
     */
    private static final UncheckedFunc<URI, Input> CACHE = new UncheckedFunc<>(
        new SyncFunc<>(
            new SoftFunc<>(
                path -> new SyncInput(
                    new StickyInput(new InputOf(path))
                )
            )
        )
    );

    /**
     * The URI.
     */
    private final URI uri;

    /**
     * Ctor.
     * @param addr URI of the index (path only)
     */
    RdIndex(final URI addr) {
        this.uri = addr;
    }

    /**
     * Get entries from the index at the URI.
     * @return List of URIs
     * @throws IOException If fails
     */
    public Iterable<String> iterate() throws IOException {
        return new XMLDocument(
            new TextOf(
                new InputWithFallback(
                    RdIndex.CACHE.apply(
                        URI.create(
                            String.format(
                                "http://datum.zerocracy.com%s/index.xml",
                                this.uri
                            )
                        )
                    ),
                    new InputOf("<index/>")
                )
            ).asString()
        ).xpath("/index/entry[@dir='false']/@uri");
    }
}
