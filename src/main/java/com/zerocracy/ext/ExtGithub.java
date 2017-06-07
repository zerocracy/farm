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
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.pmo.Pmo;
import java.io.IOException;
import org.cactoos.Scalar;

/**
 * GitHub server connector.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.7
 */
public final class ExtGithub implements Scalar<Github> {

    /**
     * PMO.
     */
    private final Project pmo;

    /**
     * Ctor.
     * @param farm Farm
     */
    public ExtGithub(final Farm farm) {
        this(new Pmo(farm));
    }

    /**
     * Ctor.
     * @param pkt Project
     */
    public ExtGithub(final Project pkt) {
        this.pmo = pkt;
    }

    @Override
    @Cacheable(forever = true)
    public Github asValue() throws IOException {
        try (final Item item = this.pmo.acq("../ext.xml")) {
            final Xocument xoc = new Xocument(item);
            return new RtGithub(
                xoc.xpath("/ext/github/login/text()").get(0),
                xoc.xpath("/ext/github/password/text()").get(0)
            );
        }
    }

}
