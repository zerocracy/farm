/**
 * Copyright (c) 2016 Zerocracy
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

import com.jcabi.xml.XML;
import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.Stakeholder;
import com.zerocracy.pm.Claims;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.xembly.Directive;

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
     * Ctor.
     * @param pkt Project
     * @param list List of stakeholders
     */
    ReactiveProject(final Project pkt, final Collection<Stakeholder> list) {
        this.origin = pkt;
        this.stakeholders = list;
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
        try (final Claims claims = new Claims(this).lock()) {
            for (final Stakeholder stk : this.stakeholders) {
                for (final XML claim : claims.find(stk.term())) {
                    claims.remove(claim.xpath("@id").get(0));
                    final Iterable<Directive> dirs = stk.process(this, claim);
                    if (dirs.iterator().hasNext()) {
                        claims.add(dirs);
                    }
                }
            }
        }
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
            if (total > 0) {
                ReactiveProject.this.run();
            }
        }
    }
}
