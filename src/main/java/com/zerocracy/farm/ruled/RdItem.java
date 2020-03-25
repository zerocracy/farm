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
package com.zerocracy.farm.ruled;

import com.zerocracy.Item;
import com.zerocracy.Project;
import com.zerocracy.farm.fake.FkItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import org.cactoos.Func;
import org.cactoos.Proc;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;

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
     * Ctor.
     * @param pkt Project
     * @param item Item
     * @param label Name of the item
     */
    RdItem(final Project pkt, final Item item, final String label) {
        this.origin = item;
        this.project = pkt;
        this.name = label;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public <T> T read(final Func<Path, T> reader) throws IOException {
        return this.origin.read(reader);
    }

    @Override
    public void update(final Proc<Path> writer) throws IOException {
        this.origin.update(
            src -> {
                final Path tmp = Files.createTempFile("rdfarm", ".xml");
                if (src.toFile().exists()) {
                    new LengthOf(new TeeInput(src, tmp)).intValue();
                }
                final FileTime time = Files.getLastModifiedTime(tmp);
                final long length = Files.size(tmp);
                writer.exec(tmp);
                this.apply(src, tmp, time, length);
            }
        );
    }

    /**
     * Apply rules to item.
     * @param src Source path
     * @param tmp Temprary path
     * @param time File time
     * @param length File length
     * @throws IOException On failure
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    private void apply(final Path src, final Path tmp,
        final FileTime time, final long length)
        throws IOException {
        try {
            final String dirty = RdItem.dirty(tmp, time, length);
            if (!dirty.isEmpty()) {
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
                Files.move(tmp, src, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            if (Files.exists(tmp)) {
                Files.delete(tmp);
            }
        }
    }

    /**
     * Is it dirty?
     * @param path Path to check
     * @param time Last access time
     * @param length File length
     * @return Some text if it's dirty
     * @throws IOException If fails
     */
    private static String dirty(final Path path,
        final FileTime time, final long length) throws IOException {
        final Collection<String> dirty = new LinkedList<>();
        if (path.toFile().length() > 0L) {
            if (!Files.getLastModifiedTime(path).equals(time)) {
                dirty.add(
                    String.format(
                        "Time:%s!=%s",
                        Files.getLastModifiedTime(path), time
                    )
                );
            }
            if (path.toFile().length() != length) {
                dirty.add(
                    String.format(
                        "Length:%s!=%s",
                        path.toFile().length(), length
                    )
                );
            }
        }
        return String.join("; ", dirty);
    }
}
