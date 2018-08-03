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
import com.jcabi.log.Logger;
import com.zerocracy.Farm;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.cactoos.Scalar;
import org.cactoos.scalar.IoCheckedScalar;
import org.cactoos.scalar.SolidScalar;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Pings.
 * <p>
 * This class sends broadcasts (pings) in the system,
 * it submit new claim with type 'Ping' every minute,
 * 'Ping hourly' every hour and 'Ping daily' every day.
 *
 * @since 1.0
 */
public final class Pings {
    /**
     * Claim job param.
     */
    private static final String CLAIM = "claim";

    /**
     * Quartz group.
     */
    private static final String GROUP = "pings";

    /**
     * Farm.
     */
    private final IoCheckedScalar<Scheduler> quartz;

    /**
     * Ctor.
     * @param farm Farm
     */
    public Pings(final Farm farm) {
        this(StdSchedulerFactory::getDefaultScheduler, farm);
    }

    /**
     * Ctor with custom scheduler.
     * @param scheduler Quartz scheduler
     * @param farm Farm
     */
    Pings(final Scalar<Scheduler> scheduler, final Farm farm) {
        this(scheduler, farm, Tv.FIVE);
    }

    /**
     * Ctor.
     * @param scheduler Quartz scheduler
     * @param farm Farm
     * @param btchs Number of batches for minute pings
     */
    Pings(final Scalar<Scheduler> scheduler, final Farm farm, final int btchs) {
        this.quartz = new IoCheckedScalar<>(
            new SolidScalar<>(
                new Pings.Quartz(scheduler, farm, new AtomicInteger(0), btchs)
            )
        );
    }

    /**
     * Start it.
     * @throws IOException If fails
     */
    public void start() throws IOException {
        try {
            this.quartz.value().start();
        } catch (final SchedulerException err) {
            throw new IOException("Failed to start", err);
        }
        this.start(
            "minute",
            "Ping",
            SimpleScheduleBuilder.repeatMinutelyForever(Tv.FIVE)
        );
        this.start(
            "hour",
            "Ping hourly",
            SimpleScheduleBuilder.repeatHourlyForever()
        );
        this.start(
            "day",
            "Ping daily",
            // @checkstyle MagicNumberCheck (1 line)
            SimpleScheduleBuilder.repeatHourlyForever(24)
        );
        Logger.info(this, "Pings started");
    }

    /**
     * Start ping with name, type and schedule.
     * @param name Job name
     * @param claim Claim type
     * @param schedule Job schedule
     * @throws IOException If fails
     */
    private void start(final String name, final String claim,
        final SimpleScheduleBuilder schedule) throws IOException {
        try {
            final Scheduler scheduler = this.quartz.value();
            final JobKey key = new JobKey(name, Pings.GROUP);
            if (!scheduler.checkExists(key)) {
                scheduler.scheduleJob(
                    JobBuilder.newJob(Ping.class)
                        .usingJobData(Pings.CLAIM, claim)
                        .withIdentity(key)
                        .requestRecovery()
                        .build(),
                    TriggerBuilder.newTrigger()
                        .forJob(key)
                        .withSchedule(schedule)
                        .startNow()
                        .build()
                );
            }
        } catch (final SchedulerException err) {
            throw new IOException(err);
        }
    }

    /**
     * Quartz scalar.
     */
    private static final class Quartz implements Scalar<Scheduler> {
        /**
         * Farm.
         */
        private final Farm frm;

        /**
         * Quartz scheduler.
         */
        private final Scalar<Scheduler> schd;

        /**
         * Counter for jobs.
         */
        private final AtomicInteger counter;

        /**
         * Batch size.
         */
        private final int batches;

        /**
         * Ctor.
         * @param scheduler Quartz scheduler
         * @param farm Farm
         * @param cnt Counter for jobs
         * @param btchs Number of batches for minute pings
         * @checkstyle ParameterNumberCheck (3 lines)
         */
        private Quartz(final Scalar<Scheduler> scheduler, final Farm farm,
            final AtomicInteger cnt, final int btchs) {
            this.schd = scheduler;
            this.frm = farm;
            this.counter = cnt;
            this.batches = btchs;
        }

        @Override
        public Scheduler value() throws Exception {
            final Scheduler scheduler = this.schd.value();
            scheduler.getContext().put("counter", this.counter);
            scheduler.setJobFactory(
                new Pings.Factory(
                    this.frm, new SimpleJobFactory(), this.batches
                )
            );
            return scheduler;
        }
    }

    /**
     * Job quartz factory.
     */
    private static final class Factory implements JobFactory {
        /**
         * Farm.
         */
        private final Farm farm;

        /**
         * Fallback factory.
         */
        private final JobFactory fallback;

        /**
         * Batch size.
         */
        private final int batches;

        /**
         * Ctor.
         * @param farm Farm
         * @param fallback Fallback factory
         * @param btchs Number of batches for minute pings
         */
        Factory(final Farm farm, final JobFactory fallback, final int btchs) {
            this.farm = farm;
            this.fallback = fallback;
            this.batches = btchs;
        }

        @Override
        public Job newJob(final TriggerFiredBundle bundle,
            final Scheduler scheduler) throws SchedulerException {
            final Job job;
            if (Ping.class.equals(bundle.getJobDetail().getJobClass())) {
                job = new Ping(this.farm, this.batches);
            } else {
                job = this.fallback.newJob(bundle, scheduler);
            }
            return job;
        }
    }
}
