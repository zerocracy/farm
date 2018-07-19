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
package com.zerocracy.farm;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Ocket;
import com.jcabi.s3.fake.FkOcket;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link S3Item}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class S3ItemTest {

    @Test
    public void modifiesFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket", "roles.xml"
        );
        final Path temp = Files.createTempFile("", "");
        try (final Item item = new S3Item(ocket, temp)) {
            new Xocument(item).bootstrap("pm/staff/roles");
            new Xocument(item).modify(
                new Directives().xpath("/roles")
                    .add("person")
                    .attr("id", "yegor256")
                    .add("role").set("ARC")
            );
        }
        try (final Item item = new S3Item(ocket, temp)) {
            MatcherAssert.assertThat(
                new Xocument(item).xpath("/roles/text()"),
                Matchers.not(Matchers.emptyIterable())
            );
        }
    }

    @Test
    public void closesNonExistingFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket-66", "orders.xml"
        );
        try (final Item item = new S3Item(ocket)) {
            item.path();
        }
    }

    @Test
    public void closesExistingFiles() throws Exception {
        final Ocket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket-69", "bans.xml"
        );
        try (final Item item = new S3Item(ocket)) {
            Files.write(item.path(), "".getBytes());
        }
    }

    @Test
    public void refreshesFilesOnServer() throws Exception {
        final FkOcket ocket = new FkOcket(
            Files.createTempDirectory("").toFile(),
            "bucket-1", "wbs.xml"
        );
        final Path temp = Files.createTempFile("", "");
        final String before;
        try (final Item item = new S3Item(ocket, temp)) {
            new Xocument(item).bootstrap("pm/scope/wbs");
            before = new Xocument(item).toString();
            new Xocument(item).modify(
                new Directives().xpath("/wbs")
                    .add("job")
                    .attr("id", "gh:yegor256/pdd#1")
                    .add("role").set("DEV").up()
                    .add("created").set("2016-12-29T09:03:21.684Z")
            );
        }
        new Ocket.Text(ocket).write(before);
        Files.setLastModifiedTime(
            ocket.file().toPath(),
            FileTime.fromMillis(Long.MAX_VALUE)
        );
        try (final Item item = new S3Item(ocket, temp)) {
            MatcherAssert.assertThat(
                new Xocument(item).nodes("/wbs[not(job)]"),
                Matchers.not(Matchers.emptyIterable())
            );
        }
    }

    /**
     * Test with ocket which simulates async work of real S3 client.
     */
    @Test
    public void handleThreadInterruptionCorrectly() throws Exception {
        final Path target = Files.createTempFile("", "");
        final Ocket okt = new OcktInterrupted(
            new FkOcket(
                Files.createTempDirectory("").toFile(),
                "bucket-1", "test.txt"
            )
        );
        try (final Item item = new S3Item(okt, target)) {
            Files.write(
                item.path(),
                new byte[]{(byte) 1, (byte) 0},
                StandardOpenOption.WRITE
            );
        }
    }

    /**
     * Ocket implementation which thread will be interrupted after first read.
     */
    private static final class OcktInterrupted implements Ocket {
        /**
         * Origin ocket.
         */
        private final Ocket origin;

        /**
         * Ctor.
         * @param origin Origin ocket
         */
        OcktInterrupted(final Ocket origin) {
            this.origin = origin;
        }

        @Override
        public Bucket bucket() {
            return this.origin.bucket();
        }

        @Override
        public String key() {
            return this.origin.key();
        }

        @Override
        public ObjectMetadata meta() throws IOException {
            return this.origin.meta();
        }

        @Override
        public boolean exists() throws IOException {
            return this.origin.exists();
        }

        @Override
        public void read(final OutputStream output)
            throws IOException {
            this.origin.read(output);
        }

        @SuppressWarnings({"ResultOfMethodCallIgnored", "PMD.EmptyCatchBlock"})
        @Override
        public void write(
            final InputStream input,
            final ObjectMetadata meta
        ) throws IOException {
            final Thread thread = new Thread(
                () -> {
                    try {
                        Thread.currentThread().interrupt();
                        input.read();
                    } catch (final IOException ignored) {
                    }
                }
            );
            thread.start();
            try {
                thread.join();
            } catch (final InterruptedException ignored) {
            }
            input.read();
        }

        @Override
        public int compareTo(final Ocket other) {
            return this.origin.compareTo(other);
        }
    }
}
