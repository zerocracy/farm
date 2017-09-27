/**
 * Copyright (c) 2016-2017 Zerocracy
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

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pool of artifacts.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
 */
final class RdSession {

    /**
     * Origin project.
     */
    private final Project project;

    /**
     * Source of items.
     */
    private final Project source;

    /**
     * Session is alive.
     */
    private final AtomicBoolean alive;

    /**
     * File lengths.
     */
    private final Map<String, Long> lengths;

    /**
     * Pool of files.
     */
    private final Map<String, Path> files;

    /**
     * Pool of items.
     */
    private final Map<String, Item> items;

    /**
     * Ctor.
     * @param pkt Project
     * @param src Source for items
     */
    RdSession(final Project pkt, final Project src) {
        this.project = pkt;
        this.source = src;
        this.alive = new AtomicBoolean(false);
        this.items = new HashMap<>(0);
        this.files = new HashMap<>(0);
        this.lengths = new HashMap<>(0);
    }

    /**
     * Acquire a file.
     * @param file Name of file
     * @return File
     * @throws IOException If fails
     */
    public Path acquire(final String file) throws IOException {
        synchronized (this.items) {
            final Path path;
            if (this.files.containsKey(file) && this.alive.get()) {
                path = this.files.get(file);
            } else {
                final Item item = this.source.acq(file);
                this.items.put(file, item);
                path = item.path();
                this.files.put(file, path);
            }
            this.lengths.put(file, path.toFile().length());
            return path;
        }
    }

    /**
     * Release it.
     * @param file File name
     * @throws IOException If fails
     */
    public void release(final String file) throws IOException {
        synchronized (this.items) {
            final Path path = this.files.get(file);
            final boolean modified = path != null
                && Files.exists(path)
                && this.lengths.get(file) != path.toFile().length();
            if (modified) {
                this.alive.set(true);
                new RdAuto(this.project, path).propagate();
                new RdRules(this.project, path).validate();
            }
            this.lengths.remove(file);
            if (this.lengths.isEmpty()) {
                for (final Item item : this.items.values()) {
                    item.close();
                }
                this.alive.set(false);
            }
        }
    }

}
