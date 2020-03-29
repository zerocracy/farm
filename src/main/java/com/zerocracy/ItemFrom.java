/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Bytes;
import org.cactoos.Func;
import org.cactoos.Proc;
import org.cactoos.Text;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.io.BytesOf;
import org.cactoos.text.TextOf;
import org.xembly.Directive;
import org.xembly.Xembler;

/**
 * Read-only item from dynamic content.
 *
 * @since 1.0
 */
public final class ItemFrom implements Item {

    /**
     * Binary content.
     */
    private final Bytes content;

    /**
     * From xembly directives.
     * @param dirs Directives
     */
    public ItemFrom(final Iterable<Directive> dirs) {
        this(() -> new Xembler(dirs).xmlQuietly());
    }

    /**
     * Item from string.
     * @param str String
     */
    public ItemFrom(final String str) {
        this(new TextOf(str));
    }

    /**
     * From UTF8 text.
     * @param text Text
     */
    public ItemFrom(final Text text) {
        this(text, StandardCharsets.UTF_8);
    }

    /**
     * From text with charset.
     * @param text Text
     * @param charset Charset
     */
    public ItemFrom(final Text text, final Charset charset) {
        this(new BytesOf(text, charset));
    }

    /**
     * Primary ctor.
     * @param content Content bytes
     */
    private ItemFrom(final Bytes content) {
        this.content = content;
    }

    @Override
    public <T> T read(final Func<Path, T> reader) throws IOException {
        final Path tmp = TempFiles.INSTANCE.newFile(this, ".item");
        try {
            Files.write(tmp, this.content.asBytes());
            return new IoCheckedFunc<>(reader).apply(tmp);
        } finally {
            TempFiles.INSTANCE.dispose(tmp);
        }
    }

    @Override
    public void update(final Proc<Path> writer) {
        throw new UnsupportedOperationException("Item is readonly");
    }
}
