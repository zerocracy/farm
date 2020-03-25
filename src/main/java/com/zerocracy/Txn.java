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

import java.io.Closeable;
import java.io.IOException;

/**
 * Project's items transaction.
 *
 * @since 1.0
 */
public final class Txn implements Project, Closeable {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Ctor.
     * @param origin Origin project
     */
    public Txn(final Project origin) {
        this.origin = origin;
    }

    @Override
    public Item acq(final String file) throws IOException {
        return this.origin.acq(file);
    }

    @Override
    public String pid() throws IOException {
        return this.origin.pid();
    }

    /**
     * Commit transaction.
     * @throws IOException If fails
     * @checkstyle NonStaticMethodCheck (50 lines)
     */
    public void commit() throws IOException {
        // nothing to commit
    }

    @Override
    public void close() throws IOException {
        // nothing to close
    }
}
