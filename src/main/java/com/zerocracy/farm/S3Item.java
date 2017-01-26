/**
 * Copyright (c) 2016 Zerocracy
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
import com.zerocracy.jstk.Item;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Item in S3.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 * @todo #6:30min We don't want to upload objects to S3 every time. We have
 *  to check first, whether any changes were actually made. Only if they
 *  were made, we will upload. Otherwise, just delete the file and that's
 *  it.
 */
final class S3Item implements Item {

    /**
     * S3 ocket.
     */
    private final Ocket ocket;

    /**
     * File.
     */
    private final AtomicReference<Path> temp;

    /**
     * Ctor.
     * @param okt Ocket
     */
    S3Item(final Ocket okt) {
        this.ocket = okt;
        this.temp = new AtomicReference<>();
    }

    @Override
    public Path path() throws IOException {
        if (this.temp.get() == null) {
            final Path path = Paths.get("/tmp/zerocracy", this.ocket.key());
            if (path.getParent().toFile().mkdirs()) {
                Logger.info(
                    this, "Directory created for %s",
                    path.toFile().getAbsolutePath()
                );
            }
            this.temp.set(path);
            if (this.ocket.exists()) {
                final ObjectMetadata meta = this.ocket.meta();
                if (!Files.exists(path)
                    || meta.getLastModified().compareTo(
                        new Date(Files.getLastModifiedTime(path).toMillis())
                    ) > 0
                ) {
                    this.ocket.read(
                        Files.newOutputStream(
                            path,
                            StandardOpenOption.CREATE
                        )
                    );
                    Files.setLastModifiedTime(
                        path,
                        FileTime.fromMillis(meta.getLastModified().getTime())
                    );
                    Logger.info(
                        this, "Loaded %d bytes from %s",
                        path.toFile().length(),
                        this.ocket.key()
                    );
                }
            }
        }
        return this.temp.get();
    }

    @Override
    public void close() throws IOException {
        if (this.temp.get() != null) {
            if (!this.ocket.exists()
                || this.ocket.meta().getLastModified().compareTo(
                    new Date(
                        Files.getLastModifiedTime(
                            this.temp.get()
                        ).toMillis()
                    )
                ) < 0) {
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(this.temp.get().toFile().length());
                this.ocket.write(Files.newInputStream(this.temp.get()), meta);
                Logger.info(
                    this, "Saved %d bytes to %s",
                    this.temp.get().toFile().length(),
                    this.ocket.key()
                );
            }
            this.temp.set(null);
        }
    }

}
