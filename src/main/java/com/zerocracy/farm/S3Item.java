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
import com.jcabi.log.Logger;
import com.jcabi.s3.Ocket;
import com.zerocracy.Item;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.EqualsAndHashCode;
import org.cactoos.io.BytesOf;
import org.cactoos.io.InputOf;

/**
 * Item in S3.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(of = {"ocket", "temp"})
final class S3Item implements Item {

    /**
     * S3 ocket.
     */
    private final Ocket ocket;

    /**
     * File.
     */
    private final Path temp;

    /**
     * Is it open/acquired?
     */
    private final AtomicBoolean open;

    /**
     * Ctor.
     * @param okt Ocket
     * @throws IOException If fails
     */
    S3Item(final Ocket okt) throws IOException {
        this(okt, Files.createTempDirectory("").resolve(okt.key()));
    }

    /**
     * Ctor.
     * @param okt Ocket
     * @param tmp Path
     */
    S3Item(final Ocket okt, final Path tmp) {
        this.ocket = okt;
        this.temp = tmp;
        this.open = new AtomicBoolean(false);
    }

    @Override
    public String toString() {
        return this.ocket.toString();
    }

    @Override
    public Path path() throws IOException {
        if (!this.open.get()) {
            if (this.temp.getParent().toFile().mkdirs()) {
                Logger.info(
                    this, "Directory created for %s",
                    this.temp.toFile().getAbsolutePath()
                );
            }
            if (this.ocket.exists() && (!this.temp.toFile().exists()
                || this.expired())) {
                final long start = System.currentTimeMillis();
                this.ocket.read(
                    Files.newOutputStream(
                        this.temp,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                );
                Files.setLastModifiedTime(
                    this.temp,
                    FileTime.fromMillis(
                        this.ocket.meta().getLastModified().getTime()
                    )
                );
                Logger.info(
                    this, "Loaded %d bytes from %s to %s (%s) in %[ms]s",
                    this.temp.toFile().length(),
                    this.ocket.key(),
                    this.temp,
                    Files.getLastModifiedTime(this.temp),
                    System.currentTimeMillis() - start
                );
            }
            this.open.set(true);
        }
        return this.temp;
    }

    @Override
    public void close() throws IOException {
        if (this.open.get() && this.temp.toFile().exists()
            && (!this.ocket.exists() || this.dirty())) {
            final ObjectMetadata meta = new ObjectMetadata();
            final long start = System.currentTimeMillis();
            meta.setContentLength(this.temp.toFile().length());
            this.ocket.write(
                new ByteArrayInputStream(
                    new BytesOf(
                        new InputOf(this.temp)
                    ).asBytes()
                ),
                meta
            );
            Files.setLastModifiedTime(
                this.temp,
                FileTime.fromMillis(
                    this.ocket.meta().getLastModified().getTime()
                )
            );
            Logger.info(
                this, "Saved %d bytes to %s from %s (%s) in %[ms]s",
                this.temp.toFile().length(),
                this.ocket.key(),
                this.temp,
                Files.getLastModifiedTime(this.temp),
                System.currentTimeMillis() - start
            );
        }
        this.open.set(false);
    }

    /**
     * Local version is expired?
     * @return TRUE if local one is expired
     * @throws IOException If fails
     */
    private boolean expired() throws IOException {
        final Date local = new Date(
            Files.getLastModifiedTime(this.temp).toMillis()
        );
        final Date remote = this.ocket.meta().getLastModified();
        return remote.compareTo(local) > 0;
    }

    /**
     * Do we really need to save it now to S3?
     * @return TRUE if it has to be uploaded
     * @throws IOException If fails
     */
    private boolean dirty() throws IOException {
        final Date local = new Date(
            Files.getLastModifiedTime(this.temp).toMillis()
        );
        final ObjectMetadata meta = this.ocket.meta();
        final Date remote = meta.getLastModified();
        return !remote.equals(local)
            || this.temp.toFile().length() != meta.getContentLength();
    }
}
