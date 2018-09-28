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

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.ListOutcome;
import com.zerocracy.Item;
import com.zerocracy.Xocument;
import com.zerocracy.cash.Cash;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.farm.fake.FkProject;
import com.zerocracy.farm.props.PropsFarm;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link PgLedger}.
 *
 * @since 1.0
 * @todo #1662:30min Run this test with embedded postgres installation
 *  on Travis and Rultor. Embedded database should be started before tests
 *  and updated with liquibase changelog from maven.
 * @checkstyle MagicNumberCheck (500 lines)
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle LineLength (500 line)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings(
    {
        "PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals"
    }
)
public final class PgLedgerITCase {

    /**
     * Transaction id column.
     */
    private static final String TID = "id";

    /**
     * Parent column.
     */
    private static final String PARENT = "parent";

    /**
     * Project column.
     */
    private static final String PROJECT = "project";

    /**
     * Amount column.
     */
    private static final String AMOUNT = "amount";

    /**
     * Debit column.
     */
    private static final String DEBIT = "dt";

    /**
     * Debit details column.
     */
    private static final String DTX = "dtx";

    /**
     * Credit column.
     */
    private static final String CREDIT = "ct";

    /**
     * Credit details column.
     */
    private static final String CTX = "ctx";

    /**
     * Details column.
     */
    private static final String DETAILS = "details";

    /**
     * Created column (timestamp).
     */
    private static final String CREATED = "created";

    @BeforeClass
    public static void check() {
        Assume.assumeNotNull(System.getProperty("pgsql.port"));
    }

    @Test
    public void addTransaction() throws Exception {
        final FkProject pkt = new FkProject();
        final DataSource data = new ExtDataSource(new PropsFarm()).value();
        PgLedgerITCase.cleanup(data);
        final PgLedger ledger = new PgLedger(data, pkt);
        final String dtone = "d1";
        final String dtxone = "d1x";
        final String ctone = "c1";
        final String ctxone = "c1x";
        final String detailsone = "test1";
        final String dttwo = "d2";
        final String dtxtwo = "d2x";
        final String cttwo = "c2";
        final String ctxtwo = "c2x";
        final String detailstwo = "test2";
        ledger.add(
            new Ledger.Transaction(new Cash.S("$1"), dtone, dtxone, ctone, ctxone, detailsone),
            new Ledger.Transaction(new Cash.S("$2"), dttwo, dtxtwo, cttwo, ctxtwo, detailstwo)
        );
        final List<Map<String, String>> select = new JdbcSession(data)
            .sql("SELECT id, parent, project, amount, dt, dtx, ct, ctx, details FROM ledger")
            .select(
                new ListOutcome<>(
                    rset -> new MapOf<String, String>(
                        new MapEntry<>(PgLedgerITCase.TID, rset.getString(1)),
                        new MapEntry<>(PgLedgerITCase.PARENT, rset.getString(2)),
                        new MapEntry<>(PgLedgerITCase.PROJECT, rset.getString(3)),
                        new MapEntry<>(PgLedgerITCase.AMOUNT, rset.getBigDecimal(4).toString()),
                        new MapEntry<>(PgLedgerITCase.DEBIT, rset.getString(5)),
                        new MapEntry<>(PgLedgerITCase.DTX, rset.getString(6)),
                        new MapEntry<>(PgLedgerITCase.CREDIT, rset.getString(7)),
                        new MapEntry<>(PgLedgerITCase.CTX, rset.getString(8)),
                        new MapEntry<>(PgLedgerITCase.DETAILS, rset.getString(9))
                    )
                )
            );
        MatcherAssert.assertThat(
            select,
            new IsIterableContainingInOrder<Map<String, String>>(
                Arrays.asList(
                    Matchers.allOf(
                        Arrays.asList(
                            Matchers.hasEntry(PgLedgerITCase.TID, "1"),
                            Matchers.hasEntry(PgLedgerITCase.AMOUNT, "1"),
                            Matchers.hasEntry(PgLedgerITCase.PROJECT, pkt.pid()),
                            Matchers.hasEntry(PgLedgerITCase.DEBIT, dtone),
                            Matchers.hasEntry(PgLedgerITCase.DTX, dtxone),
                            Matchers.hasEntry(PgLedgerITCase.CREDIT, ctone),
                            Matchers.hasEntry("ctx", ctxone),
                            Matchers.hasEntry("details", detailsone)
                        )
                    ),
                    Matchers.allOf(
                        Arrays.asList(
                            Matchers.hasEntry(PgLedgerITCase.TID, "2"),
                            Matchers.hasEntry(PgLedgerITCase.PARENT, "1"),
                            Matchers.hasEntry(PgLedgerITCase.AMOUNT, "2"),
                            Matchers.hasEntry(PgLedgerITCase.PROJECT, pkt.pid()),
                            Matchers.hasEntry(PgLedgerITCase.DEBIT, dttwo),
                            Matchers.hasEntry(PgLedgerITCase.DTX, dtxtwo),
                            Matchers.hasEntry(PgLedgerITCase.CREDIT, cttwo),
                            Matchers.hasEntry("ctx", ctxtwo),
                            Matchers.hasEntry("details", detailstwo)
                        )
                    )
                )
            )
        );
    }

    @Test
    public void migrateFromXmlFormat() throws Exception {
        final DataSource data = new ExtDataSource(new PropsFarm()).value();
        final FkProject pkt = new FkProject();
        final Ledger ledger = new Ledger(new PropsFarm(), pkt);
        PgLedgerITCase.cleanup(data);
        final String createdone = "2018-01-01T14:49:08.061Z";
        final String createdtwo = "2018-01-02T14:49:08.061Z";
        final String assets = "assets";
        final String cash = "cash";
        final String income = "income";
        final String cus = "cus_123";
        final String detailsone = "PgLedgerITCase#migrateFromXmlFormat: funded for test";
        final String detailstwo = "PgLedgerITCase#migrateFromXmlFormat: bug was reported";
        try (Item item = pkt.acq("ledger.xml")) {
            final String amountone = "$42.11";
            final String amounttwo = "$0.01";
            final String eltr = "transaction";
            new Xocument(item.path()).bootstrap("pm/cost/ledger").modify(
                new Directives()
                    .xpath("ledger")
                    .add("transactions")
                    .push()
                    .add(eltr)
                    .attr(PgLedgerITCase.TID, 1)
                    .add(PgLedgerITCase.CREATED).set(createdone).up()
                    .add(PgLedgerITCase.AMOUNT).set(amountone).up()
                    .add(PgLedgerITCase.DEBIT).set(assets).up()
                    .add(PgLedgerITCase.DTX).set(cash).up()
                    .add(PgLedgerITCase.CREDIT).set(income).up()
                    .add(PgLedgerITCase.CTX).set(cus).up()
                    .add(PgLedgerITCase.DETAILS).set(detailsone).up()
                    .pop()
                    .push()
                    .add(eltr)
                    .attr(PgLedgerITCase.TID, 2)
                    .attr(PgLedgerITCase.PARENT, 1)
                    .add(PgLedgerITCase.CREATED).set(createdtwo).up()
                    .add(PgLedgerITCase.AMOUNT).set(amounttwo).up()
                    .add(PgLedgerITCase.DEBIT).set("liabilities").up()
                    .add(PgLedgerITCase.DTX).set("debt").up()
                    .add(PgLedgerITCase.CREDIT).set(assets).up()
                    .add(PgLedgerITCase.CTX).set(cash).up()
                    .add(PgLedgerITCase.DETAILS).set(detailstwo).up()
                    .pop()
            );
        }
        ledger.bootstrap();
        try (Item item = pkt.acq("ledger.xml")) {
            MatcherAssert.assertThat(
                "Didn't remove xml entries",
                new Xocument(item.path()).xpath("/transactions"),
                Matchers.emptyIterable()
            );
        }
        final List<Map<String, String>> select = new JdbcSession(data)
            .sql("SELECT id, parent, project, amount, dt, dtx, ct, ctx, details, created FROM ledger")
            .select(
                new ListOutcome<>(
                    rset -> new MapOf<String, String>(
                        new MapEntry<>(PgLedgerITCase.TID, rset.getString(1)),
                        new MapEntry<>(PgLedgerITCase.PARENT, rset.getString(2)),
                        new MapEntry<>(PgLedgerITCase.PROJECT, rset.getString(3)),
                        new MapEntry<>(PgLedgerITCase.AMOUNT, rset.getBigDecimal(4).toString()),
                        new MapEntry<>(PgLedgerITCase.DEBIT, rset.getString(5)),
                        new MapEntry<>(PgLedgerITCase.DTX, rset.getString(6)),
                        new MapEntry<>(PgLedgerITCase.CREDIT, rset.getString(7)),
                        new MapEntry<>(PgLedgerITCase.CTX, rset.getString(8)),
                        new MapEntry<>(PgLedgerITCase.DETAILS, rset.getString(9)),
                        new MapEntry<>(PgLedgerITCase.CREATED, rset.getTimestamp(10).toInstant().toString())
                    )
                )
            );
        MatcherAssert.assertThat(
            select,
            new IsIterableContainingInOrder<Map<String, String>>(
                Arrays.asList(
                    Matchers.allOf(
                        Arrays.asList(
                            Matchers.hasEntry(PgLedgerITCase.TID, "1"),
                            Matchers.hasEntry(PgLedgerITCase.PROJECT, pkt.pid()),
                            Matchers.hasEntry(PgLedgerITCase.CREATED, createdone),
                            Matchers.hasEntry(PgLedgerITCase.AMOUNT, "42.11"),
                            Matchers.hasEntry(PgLedgerITCase.DEBIT, assets),
                            Matchers.hasEntry(PgLedgerITCase.DTX, cash),
                            Matchers.hasEntry(PgLedgerITCase.CREDIT, income),
                            Matchers.hasEntry(PgLedgerITCase.CTX, cus),
                            Matchers.hasEntry(PgLedgerITCase.DETAILS, detailsone)
                        )
                    ),
                    Matchers.allOf(
                        Arrays.asList(
                            Matchers.hasEntry(PgLedgerITCase.TID, "2"),
                            Matchers.hasEntry(PgLedgerITCase.PROJECT, pkt.pid()),
                            Matchers.hasEntry(PgLedgerITCase.PARENT, "1"),
                            Matchers.hasEntry(PgLedgerITCase.CREATED, createdtwo),
                            Matchers.hasEntry(PgLedgerITCase.AMOUNT, "0.01"),
                            Matchers.hasEntry(PgLedgerITCase.DEBIT, "liabilities"),
                            Matchers.hasEntry(PgLedgerITCase.DTX, "debt"),
                            Matchers.hasEntry(PgLedgerITCase.CREDIT, assets),
                            Matchers.hasEntry(PgLedgerITCase.CTX, cash),
                            Matchers.hasEntry(PgLedgerITCase.DETAILS, detailstwo)
                        )
                    )
                )
            )
        );
    }

    private static void cleanup(final DataSource data) throws SQLException {
        new JdbcSession(data).sql("DELETE FROM ledger").execute();
    }
}
