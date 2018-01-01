/**
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
package com.zerocracy.pm.cost;

import com.zerocracy.Xocument;
import com.zerocracy.jstk.Item;
import com.zerocracy.jstk.Project;
import com.zerocracy.jstk.SoftException;
import com.zerocracy.jstk.cash.Cash;
import com.zerocracy.jstk.cash.CashParsingException;
import java.io.IOException;
import org.cactoos.collection.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Cost estimates.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class Estimates {

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param pkt Project
     */
    public Estimates(final Project pkt) {
        this.project = pkt;
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Estimates bootstrap() throws IOException {
        try (final Item wbs = this.item()) {
            final Xocument xoc = new Xocument(wbs.path());
            xoc.bootstrap("pm/cost/estimates");
            xoc.modify(
                new Directives()
                    .xpath("/estimates[not(@total)]")
                    .attr("total", Cash.ZERO)
            );
        }
        return this;
    }

    /**
     * Total estimate.
     * @return Total estimate
     * @throws IOException If fails
     */
    public Cash total() throws IOException {
        try (final Item wbs = this.item()) {
            return new Cash.S(
                new Xocument(wbs.path()).xpath("/estimates/@total").get(0)
            );
        }
    }

    /**
     * Update job estimate by factor.
     * @param job The job to estimate
     * @param factor The factor to use
     * @throws IOException If fails
     */
    public void boost(final String job, final double factor)
        throws IOException {
        if (this.exists(job)) {
            final double mul = 10000.0d;
            this.update(
                job,
                this.get(job)
                    .mul((long) (factor * mul))
                    .div((long) mul)
            );
        }
    }

    /**
     * Change job estimate.
     * @param job The job to estimate
     * @param cash Value
     * @throws IOException If fails
     */
    public void update(final String job, final Cash cash)
        throws IOException {
        if (this.total()
            .compareTo(new Ledger(this.project).bootstrap().cash()) > 0) {
            throw new SoftException(
                "Not enough funds available in the project"
            );
        }
        try (final Item wbs = this.item()) {
            final Xocument xoc = new Xocument(wbs.path());
            xoc.modify(
                new Directives()
                    .xpath(String.format("/estimates/order[@id= '%s']", job))
                    .remove()
                    .xpath("/estimates")
                    .add("order")
                    .attr("id", job)
                    .add("created").set(new DateAsText().asString()).up()
                    .add("cash")
                    .set(cash)
            );
            xoc.modify(
                new Directives().xpath("/estimates").attr(
                    "total",
                    new IoCheckedScalar<>(
                        new Reduced<Cash, Cash>(
                            Cash.ZERO,
                            Cash::add,
                            new Mapped<>(
                                Cash.S::new,
                                xoc.xpath("//order/cash/text()")
                            )
                        )
                    ).value()
                )
            );
        }
    }

    /**
     * Get job estimate.
     * @param job The job
     * @return The estimate
     * @throws IOException If fails of it there is no assignee
     */
    public Cash get(final String job) throws IOException {
        if (!this.exists(job)) {
            throw new SoftException(
                String.format(
                    "Job `%s` is not estimated yet",
                    job
                )
            );
        }
        try (final Item wbs = this.item()) {
            return new Cash.S(
                new Xocument(wbs.path()).xpath(
                    String.format(
                        "/estimates/order[@id='%s']/cash/text()", job
                    )
                ).get(0)
            );
        } catch (final CashParsingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Estimate exists?
     * @param job The job
     * @return TRUE if estimated
     * @throws IOException If fails of it there is no assignee
     */
    public boolean exists(final String job) throws IOException {
        try (final Item wbs = this.item()) {
            return !new Xocument(wbs.path()).nodes(
                String.format("/estimates/order[@id='%s']", job)
            ).isEmpty();
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("estimates.xml");
    }

}
