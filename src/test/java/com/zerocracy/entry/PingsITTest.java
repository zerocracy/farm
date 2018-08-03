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
package com.zerocracy.entry;

import com.jcabi.aspects.Tv;
import com.jcabi.xml.XML;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimIn;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.farm.fake.FkFarm;
import com.zerocracy.pmo.Catalog;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.cactoos.list.ListOf;
import org.cactoos.list.Mapped;
import org.cactoos.scalar.And;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Integration test for quartz with real database.
 * To start it replace PG_* constants with real connection parameters.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class PingsITTest {
    /**
     * Database URL.
     */
    private static final String PG_URL =
        "jdbc:postgresql://localhost/farm_quartz";
    /**
     * Database username.
     */
    private static final String PG_USER = "username";
    /**
     * Database password.
     */
    private static final String PG_PASS = "password";

    @Test
    @Ignore
    @SuppressWarnings(
        {"PMD.UseVarargs", "PMD.AvoidInstantiatingObjectsInLoops"}
    )
    public void restartWithDatabase() throws Exception {
        final Properties prop = new Properties();
        new And(
            (String[] args) -> prop.setProperty(args[0], args[1]),
            new Mapped<>(
                (String line) -> line.split("="),
                new ListOf<>(
                    // @checkstyle LineLengthCheck (10 line)
                    "org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool",
                    "org.quartz.threadPool.threadCount=1",
                    "org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX",
                    "org.quartz.jobStore.useProperties=false",
                    "org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate",
                    "org.quartz.jobStore.dataSource=pg",
                    "org.quartz.jobStore.tablePrefix=qrtz_",
                    "org.quartz.dataSource.pg.driver=org.postgresql.Driver",
                    String.format(
                        "org.quartz.dataSource.pg.URL=%s",
                        PingsITTest.PG_URL
                    ),
                    String.format(
                        "org.quartz.dataSource.pg.user=%s",
                        PingsITTest.PG_USER
                    ),
                    String.format(
                        "org.quartz.dataSource.pg.password=%s",
                        PingsITTest.PG_PASS
                    ),
                    "org.quartz.dataSource.pg.maxConnections=5"
                )
            )
        ).value();
        final FkFarm farm = new FkFarm();
        final Project prj = farm.find("FAKEPRJCT").iterator().next();
        new Catalog(farm).bootstrap()
            .add(prj.pid(), "2017/10/000000100/");
        final Pings pings = new Pings(
            () -> new StdSchedulerFactory(prop).getScheduler(),
            farm
        );
        pings.start();
        final Date start = new Date();
        final ClaimsItem claims = new ClaimsItem(prj).bootstrap();
        boolean get = false;
        while (
            new Date().getTime() - start.getTime()
                < TimeUnit.MINUTES.toMillis(2L)
        ) {
            for (final XML xml : claims.iterate()) {
                if ("Ping".equals(new ClaimIn(xml).type())) {
                    get = true;
                    break;
                }
            }
            if (get) {
                break;
            }
            Thread.sleep((long) Tv.HUNDRED);
        }
        MatcherAssert.assertThat(get, Matchers.is(true));
    }
}
