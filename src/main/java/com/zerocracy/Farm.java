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

import java.io.Closeable;
import java.io.IOException;

/**
 * Farm of projects.
 *
 * <p>The farm is the main storage of objects. To find the project
 * you have to provide XPath query for it. All projects are stored
 * in a catalog, which is described at catalog.xsd.</p>
 *
 * @see <a href="https://github.com/zerocracy/datum/blob/master/xsd/pmo/catalog.xsd">catalog.xsd</a>
 * @since 1.0
 */
public interface Farm extends Closeable {

    /**
     * Find all suitable projects by XPath term.
     *
     * <p>XPath query will be executed at the catalog.xml
     * and all found projects will be returned. An example query
     * may look like this: {@code @id='C3FFK3YAY'}, where {@code C3FFK3YAY}
     * is the ID of Slack channel, where the project is managed.</p>
     *
     * @param xpath XPath term
     * @return Projects found
     * @throws IOException If fails on I/O
     * @see <a href="https://github.com/zerocracy/datum/blob/master/xsd/pmo/catalog.xsd">catalog.xsd</a>
     */
    Iterable<Project> find(String xpath) throws IOException;

    @Override
    default void close() throws IOException {
        throw new UnsupportedOperationException(
            "close() is not implemented, can't close this farm."
        );
    }
}
