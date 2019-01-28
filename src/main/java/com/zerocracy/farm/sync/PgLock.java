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

import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import com.jcabi.log.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import javax.sql.DataSource;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.func.UncheckedProc;
import org.cactoos.scalar.UncheckedScalar;

/**
 * {@link Lock} using postgres table.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.TooManyMethods")
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
     * Project id.
     */
    private final String pid;

    /**
     * Resource to be locked.
     */
    private final String res;

    /**
     * Thread holder.
     */
    private final PgLock.Holder holder;

    /**
     * Ctor.
     *
     * @param data Data Database
     * @param pid Project id
     * @param res Resource to be locked
     * @param holder Thread holder
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    PgLock(final DataSource data, final String pid, final String res,
        final PgLock.Holder holder) {
        this.data = data;
        this.pid = pid;
        this.res = res;
        this.holder = holder;
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
            if (Logger.isDebugEnabled(this)) {
                Logger.debug(this, "attempting to lock");
            }
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
            locked = this.acq(session);
            if (!locked) {
                TimeUnit.MILLISECONDS.sleep(1L);
            }
        }
        return locked;
    }

    @Override
    public void unlock() {
        this.holder.free(
            none -> new JdbcSession(this.data)
                // @checkstyle LineLength (1 line)
                .sql("DELETE FROM farm_locks WHERE project = ? AND resource = ?")
                .set(this.pid)
                .set(this.res)
                .update(Outcome.VOID)
        );
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException(
            "newCondition() is not implemented"
        );
    }

    @Override
    public String toString() {
        return String.format(
            "PgLock[%s](%s:%s)", this.holder, this.pid, this.res
        );
    }

    /**
     * Acquire a lock.
     *
     * @param session Database session
     * @return True if success
     */
    private boolean acq(final JdbcSession session) {
        return this.holder.lock(
            () -> session
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO farm_locks (project, resource) VALUES (?, ?) ON CONFLICT DO NOTHING RETURNING 1")
                .set(this.pid)
                .set(this.res)
                .select(Outcome.NOT_EMPTY)
        );
    }

    /**
     * Thread holder.
     */
    public static final class Holder {

        /**
         * Lock thread reference.
         */
        private final AtomicReference<Thread> ref =
            new AtomicReference<>();

        /**
         * Lock counter.
         */
        private final AtomicInteger cnt = new AtomicInteger();

        /**
         * Sync object.
         */
        private final Object sync = new Object();

        /**
         * Acquire a lock.
         *
         * @param func Function to perform lock
         * @return True if success
         */
        public boolean lock(final Scalar<Boolean> func) {
            final Thread thread = Thread.currentThread();
            final boolean locked;
            if (this.ref.compareAndSet(thread, thread)) {
                this.cnt.incrementAndGet();
                locked = true;
            } else {
                synchronized (this.sync) {
                    locked = new UncheckedScalar<>(func).value();
                    if (locked) {
                        this.ref.set(thread);
                        this.cnt.set(1);
                    }
                }
            }
            return locked;
        }

        /**
         * Free a lock.
         *
         * @param func Function to release
         */
        public void free(final Proc<Void> func) {
            if (this.ref.get() == null) {
                final Thread thread = Thread.currentThread();
                if (this.ref.get() != thread) {
                    throw new IllegalStateException(
                        "Should be locked by same thread"
                    );
                }
                if (this.cnt.decrementAndGet() == 0) {
                    synchronized (this.sync) {
                        this.ref.set(null);
                        new UncheckedProc<>(func).exec(null);
                    }
                }
            }
        }

        @Override
        public String toString() {
            final Thread thread = this.ref.get();
            final String str;
            if (thread == null) {
                str = "free";
            } else {
                str = String.format(
                    "locked %d times by %s", this.cnt.get(), thread.getName()
                );
            }
            return str;
        }
    }
}
