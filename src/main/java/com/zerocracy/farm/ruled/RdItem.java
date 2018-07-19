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

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;

/**
 * Ruled item.
 *
 * @since 1.0
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
     * Name.
     */
    private final String name;

    /**
     * Temp file.
     */
    private final IoCheckedScalar<Path> temp;

    /**
     * Initial modification time.
     */
    private final IoCheckedScalar<FileTime> time;

    /**
     * Initial length.
     */
    private final IoCheckedScalar<Long> length;

    /**
     * Ctor.
     * @param pkt Project
     * @param item Item
     * @param label Name of the item
     */
    RdItem(final Project pkt, final Item item, final String label) {
        this.origin = item;
        this.project = pkt;
        this.name = label;
        this.temp = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> {
                    final Path tmp = Files.createTempFile("rdfarm", ".xml");
                    final Path src = this.origin.path();
                    if (src.toFile().exists()) {
                        new LengthOf(new TeeInput(src, tmp)).intValue();
                    }
                    return tmp;
                }
            )
        );
        this.time = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> Files.getLastModifiedTime(this.temp.value())
            )
        );
        this.length = new IoCheckedScalar<>(
            new SolidScalar<>(
                () -> this.temp.value().toFile().length()
            )
        );
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Path path() throws IOException {
        this.time.value();
        this.length.value();
        return this.temp.value();
    }

    @Override
    public void close() throws IOException {
        try {
            final String dirty = this.dirty();
            if (!dirty.isEmpty()) {
                final Path tmp = this.temp.value();
                final Project proxy = file -> {
                    final Item item;
                    if (this.name.equals(file)) {
                        item = new FkItem(tmp);
                    } else {
                        item = this.project.acq(file);
                    }
                    return item;
                };
                if (!"PMO".equals(this.project.pid())
                    || !"roles.xml".equals(this.name)) {
                    new RdAuto(proxy, tmp, dirty).propagate();
                    new RdRules(proxy, tmp, dirty).validate();
                }
                new LengthOf(new TeeInput(tmp, this.origin.path())).intValue();
            }
        } finally {
            this.origin.close();
        }
    }

    /**
     * Is it dirty?
     * @return Some text if it's dirty
     * @throws IOException If fails
     */
    private String dirty() throws IOException {
        final Path path = this.temp.value();
        final Collection<String> dirty = new LinkedList<>();
        if (path.toFile().length() > 0L) {
            if (!Files.getLastModifiedTime(path).equals(this.time.value())) {
                dirty.add(
                    String.format(
                        "Time:%s!=%s",
                        Files.getLastModifiedTime(path), this.time.value()
                    )
                );
            }
            if (path.toFile().length() != this.length.value()) {
                dirty.add(
                    String.format(
                        "Length:%s!=%s",
                        path.toFile().length(), this.length.value()
                    )
                );
            }
        }
        return String.join("; ", dirty);
    }

}
