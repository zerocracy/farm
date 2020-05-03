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

import com.jcabi.aspects.Tv;
import com.zerocracy.farm.guts.Guts;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cactoos.scalar.Reduced;
import org.cactoos.scalar.UncheckedScalar;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Temporary files accessor.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TempFiles implements Iterable<Directive> {

    /**
     * Instance.
     */
    public static final TempFiles INSTANCE = new TempFiles(
        new UncheckedScalar<>(() -> Files.createTempDirectory("0crat"))
            .value()
    );

    /**
     * Base dir.
     */
    private final Path base;

    /**
     * References.
     */
    private final Map<Path, Class<?>> references;

    /**
     * Primary ctor.
     * @param base Base dir
     */
    private TempFiles(final Path base) {
        this.base = base;
        this.references = new ConcurrentHashMap<>(Tv.TEN);
    }

    /**
     * Create new temp file.
     * @param owner File owner
     * @return New file
     * @throws IOException On failure
     */
    public Path newFile(final Class<?> owner)
        throws IOException {
        return this.newFile(owner, ".tmp");
    }

    /**
     * Create new temp file.
     * @param owner File owner
     * @return New file
     * @throws IOException On failure
     */
    public Path newFile(final Object owner)
        throws IOException {
        return this.newFile(owner.getClass(), ".tmp");
    }

    /**
     * Create new temp file.
     * @param owner File owner
     * @param ext File extension
     * @return New file
     * @throws IOException On failure
     */
    public Path newFile(final Object owner, final String ext)
        throws IOException {
        return this.newFile(owner.getClass(), ext);
    }

    /**
     * Create new temp file.
     * @param owner File owner
     * @param ext File extension
     * @return New file
     * @throws IOException On failure
     */
    public Path newFile(final Class<?> owner, final String ext)
        throws IOException {
        final Path file = Files.createTempFile(
            this.base, owner.getSimpleName(), ext
        );
        this.references.put(file, owner);
        return file;
    }

    /**
     * Delete temp file.
     * @param file File to delete
     * @throws IOException On failure
     */
    public void dispose(final Path file) throws IOException {
        if (Files.exists(file)) {
            Files.delete(file);
        }
        this.references.remove(file);
    }

    @Override
    public Iterator<Directive> iterator() {
        return new UncheckedScalar<>(
            new Reduced<>(
                new Directives().add("tmp-files"),
                (acc, item) -> acc.add("file")
                    .add("owner").set(item.getValue().getCanonicalName()).up()
                    .add("path").set(item.getKey()).up()
                    .up(),
                this.references.entrySet()
            )
        ).value().up().iterator();
    }

    /**
     * Farm with guts.
     */
    public static final class Farm implements com.zerocracy.Farm {

        /**
         * Origin farm.
         */
        private final com.zerocracy.Farm origin;

        /**
         * Ctor.
         * @param farm Farm
         */
        public Farm(final com.zerocracy.Farm farm) {
            this.origin = farm;
        }

        @Override
        public Iterable<Project> find(final String xpath) throws IOException {
            return new Guts(
                this.origin,
                () -> this.origin.find(xpath),
                () -> new Directives()
                    .xpath("/guts")
                    .add("farm")
                    .attr("id", "TempFiles")
                    .append(TempFiles.INSTANCE)
            ).apply(xpath);
        }

        @Override
        public void close() throws IOException {
            this.origin.close();
        }
    }
}
