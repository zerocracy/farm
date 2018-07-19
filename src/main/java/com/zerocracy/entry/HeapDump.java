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
package com.zerocracy.entry;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.InputStreamOf;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.StickyScalar;

/**
 * Heap dump in S3.
 *
 * @since 1.0
 */
public final class HeapDump {

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Path to temporary storage.
     */
    private final Path temp;

    /**
     * S3 key for heapdump.
     */
    private final IoCheckedScalar<String> key;

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @throws IOException If fails
     */
    public HeapDump(final Bucket bkt, final String pfx) throws IOException {
        this(bkt, pfx, Files.createTempDirectory(""), "heapdump.hprof");
    }

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @param tmp Storage
     * @param file File
     * @checkstyle ParameterNumber (4 lines)
     */
    public HeapDump(final Bucket bkt, final String pfx, final Path tmp,
        final String file) {
        this.bucket = bkt;
        this.temp = tmp;
        this.key = new IoCheckedScalar<>(
            new StickyScalar<>(
                () -> String.format("%s%s", pfx, file)
            )
        );
    }

    /**
     * Load dump from S3.
     * @return Dump's content
     * @throws IOException If fails
     */
    public InputStream load() throws IOException {
        final Ocket ocket = this.bucket.ocket(this.key.value());
        if (!ocket.exists()) {
            throw new IOException(
                String.format(
                    "Cannot load '%s' from S3, it doesn't exist",
                    this.key.value()
                )
            );
        }
        final File heapdump = Files.createTempDirectory("")
            .resolve("heapdump").toFile();
        try (final FileOutputStream output = new FileOutputStream(heapdump)) {
            ocket.read(output);
        }
        return new FileInputStream(heapdump);
    }

    /**
     * Save dump to S3.
     * @throws IOException If fails
     */
    public void save() throws IOException {
        final Path dump = this.temp.resolve(
            this.key.value().replaceAll("[<>:\"\\/|?*]", "_")
        );
        if (!dump.toFile().exists()) {
            throw new IOException(
                String.format(
                    "Dump file '%s' does not exist, cannot save to S3",
                    dump
                )
            );
        }
        final Ocket ocket = this.bucket.ocket(this.key.value());
        if (!ocket.exists()
            || ocket.meta().getLastModified().getTime()
            < Files.getLastModifiedTime(dump).toMillis()) {
            try (final InputStream in = new InputStreamOf(dump)) {
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(Files.size(dump));
                ocket.write(in, meta);
            }
        }
    }

}
