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
import java.nio.file.attribute.FileTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.EqualsAndHashCode;

/**
 * Ruled item.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.17
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = "origin")
final class RdItem implements Item {

    /**
     * Original item.
     */
    private final Item origin;

    /**
     * Project.
     */
    private final Project project;

    /**
     * Initial modification time.
     */
    private final AtomicReference<FileTime> time;

    /**
     * Initial length.
     */
    private final AtomicReference<Long> length;

    /**
     * Ctor.
     * @param pkt Project
     * @param item Item
     */
    RdItem(final Project pkt, final Item item) {
        this.origin = item;
        this.project = pkt;
        this.time = new AtomicReference<>(null);
        this.length = new AtomicReference<>(0L);
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        final Path path = this.origin.path();
        if (Files.exists(path)) {
            this.time.compareAndSet(null, Files.getLastModifiedTime(path));
            this.length.compareAndSet(0L, path.toFile().length());
        }
        return path;
    }

    @Override
    public void close() throws IOException {
        final Path path = this.path();
        final String dirty = this.dirty();
        if (!dirty.isEmpty()) {
            new RdAuto(this.project, path, dirty).propagate();
            new RdRules(this.project, path, dirty).validate();
        }
        this.origin.close();
    }

    /**
     * Is it dirty?
     * @return Some text if it's dirty
     * @throws IOException If fails
     */
    private String dirty() throws IOException {
        final Path path = this.path();
        final List<String> dirty = new LinkedList<>();
        if (Files.exists(path) && path.toFile().length() > 0L) {
            if (!Files.getLastModifiedTime(path).equals(this.time.get())) {
                dirty.add(
                    String.format(
                        "Time:%s!=%s",
                        Files.getLastModifiedTime(path), this.time.get()
                    )
                );
            }
            if (path.toFile().length() != this.length.get()) {
                dirty.add(
                    String.format(
                        "Length:%s!=%s",
                        path.toFile().length(), this.length.get()
                    )
                );
            }
        }
        return String.join("; ", dirty);
    }

}
