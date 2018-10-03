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

import com.zerocracy.Farm;
import com.zerocracy.Project;
import com.zerocracy.db.ExtDataSource;
import com.zerocracy.farm.props.Props;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.cactoos.BiFunc;
import org.cactoos.func.SolidBiFunc;
import org.cactoos.func.StickyFunc;
import org.cactoos.func.UncheckedBiFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.scalar.UncheckedScalar;

/**
 * Lock for farm.
 *
 * @since 1.0
 */
public final class LockOf implements Lock {

    /**
     * Lock factory.
     */
    private static final UncheckedFunc<Farm, UncheckedBiFunc<Project, String, Lock>> LOCKS =
        new UncheckedFunc<>(
            new StickyFunc<>(
                farm -> {
                    final BiFunc<Project, String, Lock> lock;
                    if (new Props(farm).has("//testing")) {
                        lock = TestLock::new;
                    } else {
                        lock = (project, resource) -> new PgLock(
                            new ExtDataSource(farm).value(), project, resource
                        );
                    }
                    return new UncheckedBiFunc<>(new SolidBiFunc<>(lock));
                }
            )
        );

    /**
     * Lock.
     */
    private final UncheckedScalar<Lock> lck;

    /**
     * Ctor.
     *
     * @param frm Farm
     */
    public LockOf(final Farm frm, final Project pkt, final String res) {
        this.lck = new UncheckedScalar<>(
            () -> LockOf.LOCKS.apply(frm).apply(pkt, res)
        );
    }

    @Override
    public void lock() {
        this.lck.value().lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        this.lck.value().lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return this.lck.value().tryLock();
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit)
        throws InterruptedException {
        return this.lck.value().tryLock(time, unit);
    }

    @Override
    public void unlock() {
        this.lck.value().unlock();
    }

    @Override
    public Condition newCondition() {
        return this.lck.value().newCondition();
    }
}
