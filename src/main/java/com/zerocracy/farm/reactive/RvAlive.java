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
package com.zerocracy.farm.reactive;

import com.zerocracy.jstk.Farm;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import org.cactoos.Scalar;
import org.cactoos.scalar.NumberOf;
import org.cactoos.text.TextOf;

/**
 * Reactive farm is still alive?
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.18
 */
public final class RvAlive implements Scalar<Boolean> {

    /**
     * Original farm.
     */
    private final Farm origin;

    /**
     * Ctor.
     * @param farm Original farm
     */
    public RvAlive(final Farm farm) {
        this.origin = farm;
    }

    @Override
    public Boolean value() throws Exception {
        final Project project = this.origin.find(
            RvFarm.class.getCanonicalName()
        ).iterator().next();
        try (final Item item = project.acq("")) {
            return new NumberOf(new TextOf(item.path())).intValue() > 0;
        }
    }

}
