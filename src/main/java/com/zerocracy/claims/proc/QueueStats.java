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
package com.zerocracy.claims.proc;

import com.amazonaws.services.sqs.model.Message;
import com.jcabi.aspects.Tv;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cactoos.Func;
import org.cactoos.Proc;
import org.cactoos.Scalar;
import org.cactoos.func.SolidFunc;
import org.cactoos.func.UncheckedFunc;

/**
 * Queue performance stats.
 *
 * @since 1.0
 */
public final class QueueStats {

    /**
     * Format.
     */
    private static final DecimalFormat FMT = new DecimalFormat();

    /**
     * Amount of seconds in one hour.
     */
    private static final double SECOND_PER_HOUR = 3600.0;

    /**
     * Instance pool.
     */
    private static final Func<String, QueueStats> POOL = new SolidFunc<>(
        pid -> new QueueStats(
            new HashMap<>(Tv.HUNDRED),
            new HashMap<>(Tv.HUNDRED),
            new HashMap<>(Tv.HUNDRED)
        )
    );

    static {
        QueueStats.FMT.setMinimumFractionDigits(0);
        QueueStats.FMT.setMaximumFractionDigits(2);
        QueueStats.FMT.setGroupingUsed(false);
    }

    /**
     * Messages by timestamp.
     */
    private final Map<Instant, Message> msgs;

    /**
     * Message delays.
     */
    private final Map<Message, Duration> delays;

    /**
     * Brigade times.
     */
    private final Map<Message, Duration> brigades;

    /**
     * Ctor.
     * @param msgs Messages
     * @param delays Delays
     * @param brigades Brigades
     */
    private QueueStats(final Map<Instant, Message> msgs,
        final Map<Message, Duration> delays,
        final Map<Message, Duration> brigades) {
        this.msgs = msgs;
        this.delays = delays;
        this.brigades = brigades;
    }

    /**
     * Add claim message.
     * @param msg Message
     */
    public void add(final Message msg) {
        synchronized (this.msgs) {
            this.cleanup();
            this.msgs.put(Instant.now(), msg);
        }
    }

    /**
     * Run brigade with message.
     * @param proc Proc to run
     * @param msg Message
     * @throws Exception If proc fails
     */
    public void runBrigade(final Proc<Message> proc, final Message msg)
        throws Exception {
        final Instant now = Instant.now();
        synchronized (this.msgs) {
            this.cleanup();
            this.msgs.entrySet().stream()
                .filter(kv -> kv.getValue().equals(msg))
                .map(Map.Entry::getKey)
                .findFirst()
                // @checkstyle LineLengthCheck (1 line)
                .ifPresent(time -> this.delays.put(msg, Duration.between(time, now)));
            proc.exec(msg);
            this.brigades.put(msg, Duration.between(now, Instant.now()));
        }
    }

    @Override
    public String toString() {
        // @checkstyle LineLengthCheck (20 lines)
        synchronized (this.msgs) {
            this.cleanup();
            return String.format(
                "S/D/BS/BL: %d/%s/%s/%s",
                this.msgs.keySet().size(),
                QueueStats.FMT.format(
                    (double) this.delays.values().stream().reduce(Duration.ZERO, Duration::plus).getSeconds()
                        / (double) this.delays.size()
                ),
                QueueStats.FMT.format(
                    (double) this.brigades.values().stream().reduce(Duration.ZERO, Duration::plus).getSeconds()
                        / (double) this.brigades.size()
                ),
                QueueStats.FMT.format(
                    (double) this.brigades.values().stream().reduce(Duration.ZERO, Duration::plus).getSeconds()
                        / QueueStats.SECOND_PER_HOUR
                )
            );
        }
    }

    /**
     * Cleanup stats.
     */
    private void cleanup() {
        synchronized (this.msgs) {
            final Instant start = Instant.now().minus(Duration.ofHours(1L));
            final List<Instant> keys = this.msgs.keySet().stream()
                .filter(time -> time.isBefore(start))
                .collect(Collectors.toList());
            keys.forEach(
                expired -> {
                    final Message msg = this.msgs.remove(expired);
                    this.delays.remove(msg);
                    this.brigades.remove(msg);
                }
            );
        }
    }

    /**
     * Extension.
     */
    public static final class Ext implements Scalar<QueueStats> {

        /**
         * Project id.
         */
        private final String pid;

        /**
         * Ctor.
         * @param pid Project id
         */
        public Ext(final String pid) {
            this.pid = pid;
        }

        @Override
        public QueueStats value() {
            return new UncheckedFunc<>(QueueStats.POOL).apply(this.pid);
        }
    }
}
