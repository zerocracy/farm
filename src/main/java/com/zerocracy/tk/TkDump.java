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
package com.zerocracy.tk;

import com.jcabi.s3.Bucket;
import com.zerocracy.entry.ExtBucket;
import com.zerocracy.entry.HeapDump;
import java.io.BufferedInputStream;
import java.io.IOException;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithBody;
import org.takes.rs.RsWithType;

/**
 * Out of memory heap dump.
 *
 * @since 1.0
 */
public final class TkDump implements Take {

    /**
     * Bucket to load heapdump from.
     */
    private final IoCheckedScalar<Bucket> bucket;

    /**
     * Ctor.
     */
    public TkDump() {
        this(new ExtBucket());
    }

    /**
     * Ctor.
     * @param bucket Bucket to load dump from.
     */
    public TkDump(final Scalar<Bucket> bucket) {
        this.bucket = new IoCheckedScalar<>(bucket);
    }

    @Override
    public Response act(final Request request) throws IOException {
        return new RsWithType(
            new RsWithBody(
                new BufferedInputStream(
                    new HeapDump(this.bucket.value(), "").load()
                )
            ),
            "application/octet-stream"
        );
    }
}
