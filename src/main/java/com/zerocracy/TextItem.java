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
import java.util.Arrays;
import java.util.List;

/**
 * Text item wrapper.
 * @since 1.0
 */
public final class TextItem {

    /**
     * Project item.
     */
    private final Item item;

    /**
     * Text charset.
     */
    private final Charset charset;

    /**
     * UTF8 text item.
     * @param item Item
     */
    public TextItem(final Item item) {
        this(item, StandardCharsets.UTF_8);
    }

    /**
     * Ctor.
     * @param item Item
     * @param charset Charset
     */
    public TextItem(final Item item, final Charset charset) {
        this.item = item;
        this.charset = charset;
    }

    /**
     * Read all file as single string.
     * @return Item content
     * @throws IOException On failure
     */
    public String readAll() throws IOException {
        return new String(
            this.item.read(Files::readAllBytes),
            this.charset
        );
    }

    /**
     * Read all lines from item.
     * @return Lines list
     * @throws IOException On failure
     */
    public List<String> readLines() throws IOException {
        return this.item.read(path -> Files.readAllLines(path, this.charset));
    }

    /**
     * Write lines to file.
     * @param lines Lines to write
     * @throws IOException On failure
     */
    public void write(final Iterable<String> lines) throws IOException {
        this.item.update(path -> Files.write(path, lines, this.charset));
    }

    /**
     * Write lines array to item.
     * @param lines Array of lines
     * @throws IOException On failure
     */
    public void write(final String... lines) throws IOException {
        this.write(Arrays.asList(lines));
    }

    /**
     * Write text to item.
     * @param text Text to write
     * @throws IOException On failure
     */
    public void write(final String text) throws IOException {
        this.item.update(
            path -> Files.write(path, text.getBytes(this.charset))
        );
    }
}
