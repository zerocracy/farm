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

import com.jcabi.jdbc.Preparation;
import com.zerocracy.Farm;
import com.zerocracy.Item;
import com.zerocracy.Par;
import com.zerocracy.Project;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import org.cactoos.Scalar;
import org.cactoos.time.DateAsText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Ledger.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public final class Ledger {

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
    public Ledger(final Farm farm, final Project pkt) {
        this.farm = farm;
        this.project = pkt;
    }

    /**
     * Is it in deficit now?
     * @return TRUE if the project doesn't have enough funds
     * @throws IOException If fails
     */
    public boolean deficit() throws IOException {
        try (final Item item = this.item()) {
            return !new Xocument(item).nodes(
                "/ledger/deficit"
            ).isEmpty();
        }
    }

    /**
     * Set deficit status.
     * @param def TRUE if there is a deficit
     * @throws IOException If fails
     */
    public void deficit(final boolean def) throws IOException {
        try (final Item item = this.item()) {
            if (def) {
                new Xocument(item).modify(
                    new Directives()
                        .xpath("/ledger[not(deficit)]")
                        .strict(1)
                        .add("deficit")
                        .set(new DateAsText().asString())
                );
            } else {
                new Xocument(item).modify(
                    new Directives()
                        .xpath("/ledger/deficit")
                        .strict(1)
                        .remove()
                );
            }
        }
    }

    /**
     * What's left in project cash?
     * @return Cash left
     * @throws IOException If fails
     */
    public Cash cash() throws IOException {
        return this.sum("assets", "dt")
            .add(this.sum("assets", "ct").mul(-1L))
            .add(this.sum("liabilities", "ct").mul(-1L))
            .add(this.sum("liabilities", "dt"));
    }

    /**
     * Add transactions.
     * @param tns Transactions
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void add(final Ledger.Transaction... tns) throws IOException {
        try (final Item item = this.item()) {
            if (!new Props(this.farm).has("//testing")
                || System.getProperty("pgsql.port") != null) {
                new PgLedger(
                    new ExtDataSource(this.farm).value(), this.project
                ).add(tns);
            }
            final Xocument xoc = new Xocument(item);
            for (final Transaction txn : tns) {
                txn.update(xoc);
            }
        } catch (final SQLException err) {
            throw new IOException("Failed to add transaction", err);
        }
    }

    /**
     * Bootstrap it.
     * @return Itself
     * @throws IOException If fails
     */
    public Ledger bootstrap() throws IOException {
        try (final Item xml = this.item()) {
            new Xocument(xml.path()).bootstrap("pm/cost/ledger");
            if (!new Props(this.farm).has("//testing")
                || System.getProperty("pgsql.port") != null) {
                new PgLedger(
                    new ExtDataSource(this.farm).value(),
                    this.project
                ).bootstrap(xml);
            }
        } catch (final SQLException err) {
            throw new IOException(
                "Failed to bootstrap postgres ledger", err
            );
        }
        return this;
    }

    /**
     * Summarize balance lines.
     * @param acc The account
     * @param col The column (either "ct" or "dt")
     * @return The sum
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Cash sum(final String acc, final String col) throws IOException {
        try (final Item item = this.item()) {
            final Iterable<String> values = new Xocument(item).xpath(
                String.format(
                    "//balance/account[name='%s']/%s/text()",
                    acc, col
                )
            );
            Cash sum = Cash.ZERO;
            for (final String val : values) {
                sum = sum.add(new Cash.S(val));
            }
            return sum;
        }
    }

    /**
     * The item.
     * @return Item
     * @throws IOException If fails
     */
    private Item item() throws IOException {
        return this.project.acq("ledger.xml");
    }

    /**
     * A transaction.
     */
    public static final class Transaction
        implements Scalar<Iterable<Directive>> {
        /**
         * The amount.
         */
        private final Cash amount;
        /**
         * The debit.
         */
        private final String debit;
        /**
         * The debit details.
         */
        private final String debitx;
        /**
         * The credit.
         */
        private final String credit;
        /**
         * The credit details.
         */
        private final String creditx;
        /**
         * The details.
         */
        private final String details;
        /**
         * Ctor.
         * @param amt Amount
         * @param dbt Debit
         * @param dbtx Debit details
         * @param cdt Credit
         * @param cdtx Credit details
         * @param text Details
         * @checkstyle ParameterNumberCheck (5 lines)
         */
        public Transaction(final Cash amt, final String dbt, final String dbtx,
            final String cdt, final String cdtx, final String text) {
            this.amount = amt;
            this.debit = dbt;
            this.debitx = dbtx;
            this.credit = cdt;
            this.creditx = cdtx;
            this.details = text;
        }
        @Override
        public Iterable<Directive> value() {
            return new Directives()
                .add("created")
                .set(new DateAsText().asString()).up()
                .add("amount").set(this.amount).up()
                .add("dt").set(this.debit).up()
                .add("dtx").set(this.debitx).up()
                .add("ct").set(this.credit).up()
                .add("ctx").set(this.creditx).up()
                .add("details")
                .set(new Par.ToText(this.details).toString())
                .up();
        }

        /**
         * Update balance.
         * @param xoc The document
         * @throws IOException If fails
         */
        public void update(final Xocument xoc) throws IOException {
            this.update(xoc, "dt", this.debit, this.debitx);
            this.update(xoc, "ct", this.credit, this.creditx);
        }

        /**
         * Update balance.
         * @param xoc The document
         * @param field Field either CT or DT
         * @param name Account name
         * @param namex Account xname
         * @throws IOException If fails
         * @checkstyle ParameterNumberCheck (5 lines)
         */
        public void update(final Xocument xoc, final String field,
            final String name, final String namex) throws IOException {
            final String xpath = String.format(
                "/ledger/balance/account[name='%s' and namex='%s']/%s",
                name, namex, field
            );
            final Cash before;
            if (xoc.nodes(xpath).isEmpty()) {
                before = Cash.ZERO;
            } else {
                before = new Cash.S(
                    xoc.xpath(String.format("%s/text()", xpath)).get(0)
                );
            }
            xoc.modify(
                new Directives()
                    .xpath("/ledger")
                    .addIf("balance")
                    .xpath(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "/ledger/balance[not(account[name='%s' and namex='%s'])]",
                            name, namex
                        )
                    )
                    .add("account")
                    .add("name").set(name).up()
                    .add("namex").set(namex).up()
                    .add("ct").set(Cash.ZERO).up()
                    .add("dt").set(Cash.ZERO).up()
                    .xpath(xpath).set(before.add(this.amount))
            );
        }

        /**
         * Preparation for SQL insert.
         *
         * @param project Project
         * @param parent Parent transaction
         * @return SQL session preparation
         * @checkstyle MagicNumberCheck (50 lines)
         */
        public Preparation preparation(final Project project,
            final Optional<Long> parent) {
            return stmt -> {
                stmt.setLong(1, parent.map(x -> x + 1L).orElse(1L));
                try {
                    stmt.setString(2, project.pid());
                } catch (final IOException err) {
                    throw new SQLException("Failed to read project id", err);
                }
                if (parent.isPresent()) {
                    stmt.setLong(3, parent.get());
                } else {
                    stmt.setNull(3, Types.BIGINT);
                }
                stmt.setBigDecimal(4, this.amount.decimal());
                stmt.setString(5, this.debit);
                stmt.setString(6, this.debitx);
                stmt.setString(7, this.credit);
                stmt.setString(8, this.creditx);
                stmt.setString(9, new Par.ToHtml(this.details).toString());
            };
        }
    }
}
