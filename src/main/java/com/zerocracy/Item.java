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
import java.nio.file.Path;
import org.cactoos.Func;
import org.cactoos.Proc;

/**
 * One item in a project.
 *
 * @since 1.0
 */
public interface Item {

    /**
     * Read item data from path.
     * <p>
     * Path is a temporary item file location which can be deleted
     * right after read.
     * </p>
     * @param reader Function to read
     * @param <T> Returned type
     * @throws IOException On failure
     * @return Future of function type
     */
    <T> T read(Func<Path, T> reader) throws IOException;

    /**
     * Update the data of item.
     * <p>
     * Path is a temporary item file location, which will be save externally
     * and removed after update.
     * </p>
     * @param writer Function to update
     * @throws IOException On failure
     */
    void update(Proc<Path> writer) throws IOException;
}
