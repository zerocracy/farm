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

import com.google.common.collect.Iterators;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.ClaimIn;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reactive project.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
final class ReactiveProject implements Project {

    /**
     * Origin project.
     */
    private final Project origin;

    /**
     * List of stakeholders.
     */
    private final Collection<Stakeholder> stakeholders;

    /**
     * Depth.
     */
    private final AtomicInteger depth;

    /**
     * Ctor.
     * @param pkt Project
     * @param list List of stakeholders
     */
    ReactiveProject(final Project pkt, final Stakeholder... list) {
        this(pkt, Arrays.asList(list));
    }

    /**
     * Ctor.
     * @param pkt Project
     * @param list List of stakeholders
     */
    ReactiveProject(final Project pkt, final Collection<Stakeholder> list) {
        this.origin = pkt;
        this.stakeholders = list;
        this.depth = new AtomicInteger();
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public Item acq(final String file) throws IOException {
        Item item = this.origin.acq(file);
        if ("claims.xml".equals(file)) {
            item = new ReactiveProject.Itm(item);
        }
        return item;
    }

    /**
     * Run through all claims in the project.
     * @throws IOException If fails
     */
    private void run() throws IOException {
        this.depth.incrementAndGet();
        final Collection<Long> seen = new HashSet<>(0);
        int cycle = 0;
        while (this.next(seen)) {
            ++cycle;
            if (cycle > Tv.TEN) {
                Logger.warn(this, "Too many cycles");
                break;
            }
        }
        this.depth.decrementAndGet();
    }

    /**
     * Try again.
     * @param seen Numbers we've seen already
     * @return TRUE if we should check once again
     * @throws IOException If fails
     */
    private boolean next(final Collection<Long> seen) throws IOException {
        final Iterator<XML> list;
        try (final Claims claims = new Claims(this.origin).lock()) {
            list = Iterators.filter(
                claims.iterate().iterator(),
                claim -> !seen.contains(new ClaimIn(claim).number())
            );
        }
        boolean more = false;
        if (list.hasNext()) {
            final XML xml = list.next();
            final long start = System.currentTimeMillis();
            final ClaimIn claim = new ClaimIn(xml);
            for (final Stakeholder stk : this.stakeholders) {
                stk.process(this, xml);
            }
            seen.add(claim.number());
            more = true;
            Logger.info(
                this, "Seen \"%s/%d\" at \"%s\", %[ms]s",
                claim.type(), claim.number(), this.toString(),
                System.currentTimeMillis() - start
            );
        }
        return more;
    }

    /**
     * Item that triggers stakeholders.
     */
    private final class Itm implements Item {
        /**
         * Original item.
         */
        private final Item original;
        /**
         * Ctor.
         * @param item Original item
         */
        Itm(final Item item) {
            this.original = item;
        }
        @Override
        public Path path() throws IOException {
            return this.original.path();
        }
        @Override
        public void close() throws IOException {
            final int total = new Xocument(this.path())
                .nodes("/claims/claim").size();
            this.original.close();
            if (total > 0 && ReactiveProject.this.depth.get() < Tv.TEN) {
                ReactiveProject.this.run();
            }
        }
    }
}
