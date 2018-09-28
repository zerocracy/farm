/*
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

import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.SoftException;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.cash.CashParsingException;
import com.zerocracy.cash.Currency;
import com.zerocracy.pm.in.Orders;
import com.zerocracy.pm.scope.Wbs;
import com.zerocracy.pm.staff.Roles;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.collection.Mapped;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.Reduced;
import org.cactoos.scalar.Ternary;
import org.cactoos.time.DateAsText;
import org.xembly.Directives;

/**
 * Cost estimates.
 * <p>
 * Estimate is an absolute cash value (not minutes of work)
 * which will be payed for a job on complete. It represents current state of
 * <i>pending payments</i>: how much cash will be taken from project
 * funds to pay for orders.<br/>
 * For example this estimate:
 * <pre>
 * <code>&lt;order id="gh:yegor256/pdd#3"&gt;
 *   &lt;cash&gt;$15&lt;/cash&gt;
 *   &lt;created&gt;2016-12-29T09:03:21.684Z&lt;/created&gt;
 *   &lt;role&gt;REV&lt;/role&gt;
 * &lt;/order&gt;
 * </code>
 * </pre>
 * means that job {@code gh:yegor256/pdd#3} was estimated in $15 as a
 * code-review task.<br/>
 * Project estimates locks a cash from budget, so if estimated value is
 * bigger that project's cash, project will be turned into deficit mode.
 * </p>
 * <p>
 * Estimate can be cleaned from {@link com.zerocracy.farm.ruled.RdAuto},
 * see {@code 03-estimates-remove.xsl} in datum repo.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class Estimates {

    /**
     * Farm.
     */
    private final Farm farm;

    /**
     * Project.
     */
    private final Project project;

    /**
     * Ctor.
     * @param farm Farm
     * @param pkt Project
     */
    public Estimates(final Farm farm, final Project pkt) {
        this.farm = farm;
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
     * Change job estimate.
     * @param job The job to estimate
     * @param cash Value
     * @throws IOException If fails
     */
    public void update(final String job, final Cash cash)
        throws IOException {
        final Orders orders = new Orders(this.farm, this.project).bootstrap();
        if (!orders.assigned(job)) {
            throw new SoftException(
                new Par(
                    "Job %s is not assigned, can't update estimate"
                ).say(job)
            );
        }
        final Ledger ledger = new Ledger(this.farm, this.project).bootstrap();
        if (this.total().compareTo(ledger.cash()) > 0) {
            final Roles roles = new Roles(this.project).bootstrap();
            final Collection<String> owners = new LinkedList<>();
            owners.addAll(roles.findByRole("ARC"));
            if (owners.isEmpty()) {
                owners.addAll(roles.findByRole("PO"));
            }
            throw new SoftException(
                new Par(
                    "@%s not enough funds available in the project,",
                    "can't set budget of job %s, see §21;",
                    "@%s will get no money on completion;",
                    "in order to fix that, add funds to the project",
                    "and assign the job again"
                ).say(owners.iterator().next(), job, orders.performer(job))
            );
        }
        final String role = new Wbs(this.project).bootstrap().role(job);
        try (final Item estimates = this.item()) {
            final Xocument xoc = new Xocument(estimates.path());
            xoc.modify(
                new Directives()
                    .xpath(String.format("/estimates/order[@id= '%s']", job))
                    .remove()
                    .xpath("/estimates")
                    .add("order")
                    .attr("id", job)
                    .add("role").set(role).up()
                    .add("created").set(new DateAsText().asString()).up()
                    .add("cash")
                    .set(cash)
            );
            final Cash value = new IoCheckedScalar<Cash>(
                new Ternary<>(
                    new IoCheckedScalar<>(
                        new Reduced<Cash, Cash>(
                            Cash.ZERO,
                            Cash::add,
                            new Mapped<>(
                                Cash.S::new,
                                xoc.xpath("//order/cash/text()")
                            )
                        )
                    ).value(),
                    Cash::unified,
                    csh -> csh,
                    csh -> csh.exchange(Currency.USD)
                )
            ).value();
            xoc.modify(
                new Directives().xpath("/estimates").attr(
                    "total",
                    value
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
                new Par(
                    "Job `%s` is not estimated yet"
                ).say(job)
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
