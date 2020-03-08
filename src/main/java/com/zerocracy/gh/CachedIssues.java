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

package com.zerocracy.gh;

import com.jcabi.aspects.Tv;
import com.jcabi.github.Github;
import com.zerocracy.radars.github.Job;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.json.JsonObject;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.collection.Mapped;
import org.cactoos.func.StickyFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.scalar.UncheckedScalar;

/**
 * Cached github issues.
 * @since 1.0
 */
public final class CachedIssues {

    /**
     * Github client.
     */
    private final Github ghb;

    /**
     * Cache map.
     * @checkstyle LineLengthCheck (5 lines)
     */
    private final ConcurrentHashMap<String, CachedIssues.CachedValue<JsonObject>> cache;

    /**
     * Ctor.
     * @param github Github
     */
    public CachedIssues(final Github github) {
        this.ghb = github;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Issue labels.
     * @param job Ticket id
     * @return Set of label strings
     */
    public Set<String> labels(final String job) {
        return new HashSet<>(
            new Mapped<>(
                jlabel -> jlabel.asJsonObject().getString("name"),
                this.json(job).getJsonArray("labels")
            )
        );
    }

    /**
     * Check job has milestone.
     * @param job Ticket id
     * @return TRUE if has a milestone
     */
    public boolean hasMilestone(final String job) {
        return this.json(job).getJsonObject("milestone") != null;
    }

    /**
     * Cached or fetched json.
     * @param job Ticket id
     * @return JSON of issue
     */
    private JsonObject json(final String job) {
        return this.cache.computeIfAbsent(
            job,
            key -> new CachedIssues.CachedValue<>(
                () -> new Job.Issue(this.ghb, job).json()
            )
        ).value(TimeUnit.MINUTES.toMillis(Tv.TEN));
    }

    /**
     * Extension.
     */
    public static final class Ext implements Scalar<CachedIssues> {

        /**
         * Instance pool.
         */
        private static final Func<Github, CachedIssues> POOL =
            new StickyFunc<>(CachedIssues::new);

        /**
         * Farm.
         */
        private final Github ghb;

        /**
         * Ctor.
         * @param ghb Github
         */
        public Ext(final Github ghb) {
            this.ghb = ghb;
        }

        @Override
        public CachedIssues value() {
            return new UncheckedFunc<>(CachedIssues.Ext.POOL).apply(this.ghb);
        }
    }

    /**
     * Cached value.
     * @param <T> Value type
     */
    private static final class CachedValue<T> {

        /**
         * Soft reference.
         */
        private SoftReference<T> ref;

        /**
         * Created timestamp.
         */
        private long born;

        /**
         * Value loader.
         */
        private final Scalar<T> loader;

        /**
         * Ctor.
         * @param loader Item loader
         */
        CachedValue(final Scalar<T> loader) {
            this.loader = loader;
            this.ref = new SoftReference<>(null);
        }

        /**
         * Fetch the value.
         * <p>
         * Try to read value from soft reference cache if available and
         * TTL is greater than value live time.
         * </p>
         * @param ttl Time to live
         * @return Value
         */
        @SuppressWarnings("PMD.NullAssignment")
        public T value(final long ttl) {
            final long now = System.currentTimeMillis();
            T value;
            if (now - this.born < ttl) {
                value = this.ref.get();
            } else {
                this.ref.clear();
                value = null;
            }
            if (value == null) {
                synchronized (this.loader) {
                    value = this.ref.get();
                    if (value == null) {
                        value = new UncheckedScalar<>(this.loader).value();
                        this.ref = new SoftReference<>(value);
                    }
                }
            }
            return value;
        }
    }
}
