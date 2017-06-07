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
package com.zerocracy.ext;

import com.jcabi.aspects.Cacheable;
import com.jcabi.s3.Bucket;
import com.jcabi.s3.Region;
import com.jcabi.s3.retry.ReRegion;
import java.io.IOException;
import java.util.Properties;
import org.cactoos.Scalar;

/**
 * S3 Bucket.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.11
 */
public final class ExtBucket implements Scalar<Bucket> {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * Ctor.
     * @throws IOException If fails
     */
    public ExtBucket() throws IOException {
        this(new ExtProperties().asValue());
    }

    /**
     * Ctor.
     * @param pps Properties
     */
    public ExtBucket(final Properties pps) {
        this.props = pps;
    }

    @Override
    @Cacheable(forever = true)
    public Bucket asValue() {
        return new ReRegion(
            new Region.Simple(
                this.props.getProperty("s3.key"),
                this.props.getProperty("s3.secret")
            )
        ).bucket(this.props.getProperty("s3.bucket"));
    }

}
