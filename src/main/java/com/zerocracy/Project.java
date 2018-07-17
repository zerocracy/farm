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
package com.zerocracy;

import java.io.IOException;

/**
 * Project.
 * <p>
 * A project in {@link Farm}, collection of {@link Item}s.
 *
 * @since 1.0
 */
public interface Project {

    /**
     * Project ID.
     * @return PID
     * @throws IOException If fails on I/O
     * @since 1.0
     */
    default String pid() throws IOException {
        throw new UnsupportedOperationException(
            "pid() is not implemented"
        );
    }

    /**
     * Acquire an item (will be unlocked when Item is released).
     *
     * <p>Each item is a file in project container. When you acquire it
     * nobody else will have access to this file, until you call
     * {@code Item.close()}. The best approach is to use items
     * in try-with-resource blocks, for example:</p>
     *
     * <pre>{@code try (Item item = project.acq("test.xml")) {
     *   Path path = item.path();
     *   Files.write(path, "hello, world".getBytes());
     * }}</pre>
     *
     * @param file File name in the project
     * @return Item acquired
     * @throws IOException If fails on I/O
     */
    Item acq(String file) throws IOException;

}
