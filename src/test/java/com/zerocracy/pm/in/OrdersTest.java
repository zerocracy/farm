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
package com.zerocracy.pm.in;

import com.zerocracy.jstk.fake.FkProject;
import org.junit.Test;

/**
 * Test case for {@link Orders}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class OrdersTest {

    @Test
    public void assignsAndResigns() throws Exception {
        final Orders orders = new Orders(new FkProject()).bootstrap();
        final String job = "gh:yegor256/0pdd#13";
        orders.assign(job, "yegor256");
    }

}
