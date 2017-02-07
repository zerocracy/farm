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
package com.zerocracy.farm;

import com.jcabi.s3.Bucket;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

/**
 * Project in S3.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class S3Project implements Project {

    /**
     * S3 bucket.
     */
    private final Bucket bucket;

    /**
     * Path in the bucket.
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
    S3Project(final Bucket bkt, final String pfx) throws IOException {
        this(bkt, pfx, Files.createTempDirectory(""));
    }

    /**
     * Ctor.
     * @param bkt Bucket
     * @param pfx Prefix
     * @param tmp Storage
     */
    S3Project(final Bucket bkt, final String pfx, final Path tmp) {
        this.bucket = bkt;
        this.prefix = pfx;
        this.temp = tmp;
    }

    @Override
    public String toString() {
        return StringUtils.substringAfterLast(
            StringUtils.stripEnd(this.prefix, "/"),
            "/"
        );
    }

    @Override
    public Item acq(final String file) {
        if (!file.matches("[a-z\\-]+\\.[a-z]+")) {
            throw new IllegalArgumentException(
                String.format(
                    "Unacceptable file name: \"%s\"", file
                )
            );
        }
        final String key = String.format("%s%s", this.prefix, file);
        return new SyncItem(
            new S3Item(
                this.bucket.ocket(key),
                this.temp.resolve(key)
            )
        );
    }

}
