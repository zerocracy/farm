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
package com.zerocracy.pm.cost;

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import java.io.IOException;
import org.cactoos.iterable.ItemAt;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.UncheckedScalar;
import org.xembly.Directives;

/**
 * Payment boosts.
 * @author Kirill (g4s8.public@gmail.com)
 * @version $Id$
 * @since 0.16
 */
public final class Boosts {

    /**
     * Default boost factor.
     */
    private static final int FCT_DEFAULT = 2;

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Boosts(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Boost factor for a job.
     * @param job Job id
     * @return Boost factor value
     * @throws IOException If fails
     */
    public int factor(final String job) throws IOException {
        try (final Item item = this.item()) {
            return new UncheckedScalar<>(
                new ItemAt<Integer>(
                    src -> Boosts.FCT_DEFAULT,
                    new Mapped<>(
                        Integer::parseInt,
                        new Xocument(item).xpath(
                            String.format(
                                "/boosts/boost[@id='%s']/text()",
                                job
                            )
                        )
                    )
                )
            ).value();
        }
    }

    /**
     * Boost a job with specified factor.
     * @param job Job id
     * @param factor Boost factor value
     * @throws IOException If fails
     */
    public void boost(final String job, final int factor)
        throws IOException {
        if (factor == 0) {
            throw new SoftException(
                String.format(
                    "Boost factor for %s can't be zero",
                    job
                )
            );
        }
        // @checkstyle MagicNumber (1 line)
        if (factor > 64) {
            throw new SoftException(
                String.format(
                    "Boost factor for %s can't be over 64: %d",
                    job, factor
                )
            );
        }
        new Estimates(this.project).bootstrap().boost(
            job, (double) factor / (double) this.factor(job)
        );
        try (final Item item = this.item()) {
            final Xocument xoc = new Xocument(item);
            final String xpath = String.format("/boosts/boost[@id='%s']", job);
            if (xoc.nodes(xpath).isEmpty()) {
                xoc.modify(
                    new Directives()
                        .xpath("/boosts")
                        .add("boost")
                        .attr("id", job)
                        .set(factor)
                        .up()
                );
            } else {
                xoc.modify(
                    new Directives()
                        .xpath(xpath)
                        .set(factor)
                );
            }
        }
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Boosts bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            new Xocument(wbs.path()).bootstrap("pm/cost/boosts");
        }
        return this;
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("boosts.xml");
    }
}
