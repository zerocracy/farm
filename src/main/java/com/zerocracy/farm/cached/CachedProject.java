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
package com.zerocracy.farm.cached;

import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import java.io.IOException;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Cached project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.11
 */
@EqualsAndHashCode(of = "origin")
final class CachedProject implements Project {

    /**
     * Default max pool size.
     */
    private static final int DEFAULT_THRESHOLD = 50;

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * Pool of items.
     */
    private final Map<String, Item> pool;

    /**
     * Max pool size.
     */
    private final int threshold;

    /**
     * Ctor.
     * @param pkt Project
     * @param map Pool of items
     */
    CachedProject(final Project pkt, final Map<String, Item> map) {
        this(pkt, map, CachedProject.DEFAULT_THRESHOLD);
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param map Pool of items
     * @param max Max pool size
     */
    CachedProject(final Project pkt, final Map<String, Item> map,
        final int max) {
        this.origin = pkt;
        this.pool = map;
        this.threshold = max;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        final String location = String.format(
            "%s %s", this.origin, file
        );
        if (this.pool.size() > this.threshold) {
            this.pool.clear();
        }
        if (!this.pool.containsKey(location)) {
            this.pool.put(location, this.origin.acq(file));
        }
        return this.pool.get(location);
    }
}
