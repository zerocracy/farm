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
package com.zerocracy.farm.sync;

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import com.zerocracy.Project;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import javax.sql.DataSource;

/**
 * {@link Lock} using MongoDB.
 *
 * @since 1.0
 */
final class PgLock implements Lock {

    /**
     * Default time to wait.
     */
    private static final long DEFAULT_WAIT_SEC = 500L;

    /**
     * Data source.
     */
    private final DataSource data;

    /**
     * Project.
     */
    private final Project pkt;

    /**
     * Resource to be locked.
     */
    private final String res;

    /**
     * Ctor.
     *
     * @param data Data Database
     * @param pkt Project
     * @param res Resource to be locked
     */
    PgLock(final DataSource data, final Project pkt,
        final String res) {
        this.data = data;
        this.pkt = pkt;
        this.res = res;
    }

    @Override
    public void lock() {
        try {
            this.lockInterruptibly();
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (!this.tryLock(PgLock.DEFAULT_WAIT_SEC, TimeUnit.SECONDS)) {
            // wait
        }
    }

    @Override
    public boolean tryLock() {
        boolean locked = false;
        try {
            locked = this.tryLock(PgLock.DEFAULT_WAIT_SEC, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return locked;
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit)
        throws InterruptedException {
        final long deadline = System.nanoTime() + unit.toNanos(time);
        boolean locked = false;
        final JdbcSession session = new JdbcSession(this.data);
        while (!locked && System.nanoTime() < deadline) {
            try {
                locked = session
                    .sql("INSERT INTO farm_locks (project, resource) VALUES (?, ?) ON CONFLICT DO NOTHING RETURNING 1")
                    .set(this.pkt.pid())
                    .set(this.res)
                    .select(Outcome.NOT_EMPTY);
            } catch (final IOException | SQLException err) {
                throw new IllegalStateException("Failed to lock", err);
            }
            TimeUnit.MILLISECONDS.sleep(1L);
        }
        return locked;
    }

    @Override
    public void unlock() {
        try {
            new JdbcSession(this.data)
                .sql("DELETE FROM farm_locks WHERE project = ? AND resource = ?")
                .set(this.pkt.pid())
                .set(this.res)
                .update(Outcome.VOID);
        } catch (final IOException | SQLException err) {
            throw new IllegalStateException("Failed to unlock", err);
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException(
            "newCondition() is not implemented"
        );
    }
}
