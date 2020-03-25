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
package com.zerocracy.farm.fake;

import com.zerocracy.Item;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.Func;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.func.IoCheckedFunc;
import org.cactoos.func.IoCheckedProc;
import org.cactoos.io.LengthOf;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.StickyScalar;
import org.cactoos.scalar.SyncScalar;
import org.cactoos.scalar.UncheckedScalar;

/**
 * Fake {@link Item}.
 *
 * <p>There is no thread-safety guarantee.</p>
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class FkItem implements Item {

    /**
     * Location of the file.
     */
    private final Scalar<Path> file;

    /**
     * Delete on close? If false, deletes on JVM exit.
     */
    private final boolean delete;

    /**
     * Ctor.
     */
    public FkItem() {
        this(() -> Files.createTempFile("jstk", ".xml"), true);
    }

    /**
     * Ctor.
     * @param path Path of the file
     * @since 1.0
     */
    public FkItem(final Path path) {
        this(
            () -> {
                path.toFile().getParentFile().mkdirs();
                return path;
            },
            false
        );
    }

    /**
     * Ctor.
     * @param content File content to return
     * @since 1.0
     */
    public FkItem(final String content) {
        this(
            () -> {
                final Path path = Files.createTempFile("jstk-body", "");
                new LengthOf(new TeeInput(content, path)).value();
                return path;
            },
            true
        );
    }

    /**
     * Ctor.
     * @param path Path of the file
     * @param del Delete on close()?
     * @since 1.0
     */
    FkItem(final Scalar<Path> path, final boolean del) {
        this.file = new SyncScalar<>(new StickyScalar<>(path));
        this.delete = del;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof FkItem
            && this.toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return new UncheckedScalar<>(this.file).value()
            .getFileName().toString();
    }

    @Override
    public <T> T read(final Func<Path, T> reader) throws IOException {
        final Path src = new IoCheckedScalar<>(this.file).value();
        final T res = new IoCheckedFunc<>(reader).apply(src);
        this.close();
        return res;
    }

    @Override
    public void update(final Proc<Path> writer) throws IOException {
        final Path src = new IoCheckedScalar<>(this.file).value();
        new IoCheckedProc<>(writer).exec(src);
        this.close();
    }

    /**
     * Close the file.
     * @throws IOException On failure
     */
    private void close() throws IOException {
        if (this.delete) {
            Files.delete(new IoCheckedScalar<>(this.file).value());
        } else {
            new IoCheckedScalar<>(this.file).value().toFile().deleteOnExit();
        }
    }
}
