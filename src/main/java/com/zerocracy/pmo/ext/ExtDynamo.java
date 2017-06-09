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
package com.zerocracy.pmo.ext;

import com.jcabi.aspects.Cacheable;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import java.io.IOException;
import java.util.Properties;
import org.cactoos.Scalar;

/**
 * DynamoDB server connector.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
public final class ExtDynamo implements Scalar<Region> {

    /**
     * Properties.
     */
    private final Properties props;

    /**
     * Ctor.
     * @throws IOException If fails
     */
    public ExtDynamo() throws IOException {
        this(new ExtProperties().asValue());
    }

    /**
     * Ctor.
     * @param pps Properties
     */
    public ExtDynamo(final Properties pps) {
        this.props = pps;
    }

    @Cacheable(forever = true)
    public Region asValue() {
        return new ReRegion(
            new Region.Simple(
                new Credentials.Simple(
                    this.props.getProperty("dynamo.key"),
                    this.props.getProperty("dynamo.secret")
                )
            )
        );
    }

}
