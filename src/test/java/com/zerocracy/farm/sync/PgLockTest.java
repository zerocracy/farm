/*
 * Copyright (c) 2016-2019 Zerocracy
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
package com.zerocracy.farm.sync;

import com.jcabi.aspects.Tv;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.farm.props.PropsFarm;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link PgLock}.
 *
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class PgLockTest {

    @BeforeClass
    public static void setUp() {
        Assume.assumeNotNull(System.getProperty("pgsql.port"));
    }

    @Test
    public void lockResource() throws Exception {
        final String res = "roles.xml";
        final List<String> actions =
            Collections.synchronizedList(new LinkedList<>());
        final DataSource data = new ExtDataSource(new PropsFarm()).value();
        final String pid = "test";
        final Lock lone = new PgLock(data, pid, res, new PgLock.Holder());
        actions.add("locking1");
        lone.lock();
        actions.add("locked1");
        final Object started = new Object();
        final Thread thread = new Thread(
            () -> {
                synchronized (started) {
                    started.notifyAll();
                }
                final PgLock ltwo =
                    new PgLock(data, pid, res, new PgLock.Holder());
                actions.add("locking2");
                ltwo.lock();
                actions.add("locked2");
                ltwo.unlock();
            }
        );
        thread.start();
        synchronized (started) {
            started.wait();
        }
        TimeUnit.MILLISECONDS.sleep((long) Tv.HUNDRED);
        actions.add("unlocking1");
        lone.unlock();
        thread.join();
        MatcherAssert.assertThat(
            actions,
            Matchers.contains(
                "locking1", "locked1", "locking2",
                "unlocking1", "locked2"
            )
        );
    }
}
