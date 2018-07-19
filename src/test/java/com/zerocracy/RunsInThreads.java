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
package com.zerocracy;

import com.jcabi.log.VerboseCallable;
import com.jcabi.log.VerboseThreads;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.cactoos.Func;
import org.cactoos.iterable.IterableNoNulls;
import org.cactoos.iterable.Mapped;
import org.cactoos.scalar.And;
import org.cactoos.scalar.UncheckedScalar;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for {@link Func} that must run in multiple threads.
 *
 * @param <T> Type of input
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 */
public final class RunsInThreads<T> extends TypeSafeMatcher<Func<T, Boolean>> {

    /**
     * Input.
     */
    private final T input;

    /**
     * Total cid of threads to run.
     */
    private final int total;

    /**
     * Ctor.
     */
    public RunsInThreads() {
        this(null);
    }

    /**
     * Ctor.
     * @param object Input object
     */
    public RunsInThreads(final T object) {
        this(object, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Ctor.
     * @param object Input object
     */
    public RunsInThreads(final T object, final int threads) {
        super();
        this.input = object;
        this.total = threads;
    }

    @Override
    public boolean matchesSafely(final Func<T, Boolean> func) {
        final ExecutorService service = Executors.newFixedThreadPool(
            this.total, new VerboseThreads(RunsInThreads.class)
        );
        final CountDownLatch latch = new CountDownLatch(1);
        final Collection<Future<Boolean>> futures = new ArrayList<>(this.total);
        final Callable<Boolean> task = new VerboseCallable<>(
            () -> {
                latch.await();
                return func.apply(this.input);
            },
            true, true
        );
        for (int thread = 0; thread < this.total; ++thread) {
            futures.add(service.submit(task));
        }
        latch.countDown();
        final boolean matches = new UncheckedScalar<>(
            new And(
                new IterableNoNulls<>(
                    new Mapped<>(
                        future -> future::get,
                        futures
                    )
                )
            )
        ).value();
        service.shutdown();
        return matches;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("failed");
    }
}
