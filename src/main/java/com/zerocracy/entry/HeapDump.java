/**
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

import com.jcabi.s3.Bucket;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Heap dump in S3.
 *
 * @author Izbassar Tolegen (t.izbassar@gmail.com)
 * @version $Id$
 * @since 1.0
 *
 * @todo #400:30min Use this class, to periodically update
 *  contents from ./heapdump.hprof file to S3. Should
 *  be implemented after #680 would be resolved as part
 *  of the routine work, that needs to be done in background.
 */
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
public final class HeapDump {

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Path in bucket.
     */
    private final String prefix;

    /**
     * Path to temporary storage.
     */
    private final Path temp;

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @throws IOException If fails
     */
    public HeapDump(final Bucket bkt, final String pfx) throws IOException {
        this(bkt, pfx, Files.createTempDirectory(""));
    }

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @param tmp Storage
     */
    public HeapDump(final Bucket bkt, final String pfx, final Path tmp) {
        this.bucket = bkt;
        this.prefix = pfx;
        this.temp = tmp;
    }

    /**
     * Load dump from S3.
     * @return Dump's content
     * @todo #400:30min Should return the latest
     *  version of heap dump, available in S3 or
     *  temporary storage (if it is up to date).
     *  Cover implementation with unit and
     *  integration tests.
     */
    public InputStream load() {
        throw new UnsupportedOperationException(
            "HeapDump#load() not yet implemented"
        );
    }

    /**
     * Save dump to S3.
     * @todo #400:30min Should save heap dump to S3 bucket
     *  if file with dump exists, but only do that, if there
     *  is new version of file available. Ignore, if we
     *  already have latest version in S3. Should be covered
     *  with unit and integration tests.
     */
    public void save() {
        throw new UnsupportedOperationException(
            "HeapDump#save() not yet implemented"
        );
    }
}
