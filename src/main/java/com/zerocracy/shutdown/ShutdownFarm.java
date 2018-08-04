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
package com.zerocracy.shutdown;

import com.zerocracy.Farm;
import com.zerocracy.Project;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Farm which can shutdown the App.
 *
 * @since 1.0
 */
public final class ShutdownFarm implements Farm {

    /**
     * Origin farm.
     */
    private final Farm origin;

    /**
     * Shutdown hook.
     */
    private final ShutdownFarm.Hook hook;

    /**
     * Ctor.
     *
     * @param origin Origin farm
     * @param hook Shutdown hook
     */
    public ShutdownFarm(final Farm origin, final ShutdownFarm.Hook hook) {
        this.origin = origin;
        this.hook = hook;
    }

    @Override
    public Iterable<Project> find(final String xpath) throws IOException {
        return this.origin.find(xpath);
    }

    @Override
    public void close() throws IOException {
        try {
            this.hook.shutdown();
        } finally {
            this.origin.close();
        }
    }

    /**
     * Shutdown hook.
     */
    public static final class Hook {

        /**
         * Initial state.
         */
        private static final String NONE = "none";

        /**
         * Shutdown in progress.
         */
        private static final String STOPPING = "stopping";

        /**
         * Shutdown completed.
         */
        private static final String STOPPED = "stopped";

        /**
         * Current state.
         */
        private final AtomicReference<String> state;

        /**
         * Default ctor.
         */
        public Hook() {
            this(new AtomicReference<>(ShutdownFarm.Hook.NONE));
        }

        /**
         * Primary ctor.
         *
         * @param state State
         */
        Hook(final AtomicReference<String> state) {
            this.state = state;
        }

        /**
         * Request shutdown.
         */
        public void shutdown() {
            if (!this.state.compareAndSet(
                ShutdownFarm.Hook.NONE, ShutdownFarm.Hook.STOPPING
            )) {
                throw new IllegalStateException(
                    String.format("Cant stop when %s", this.state.get())
                );
            }
            while (!ShutdownFarm.Hook.STOPPED.equals(this.state.get())) {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (final InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        /**
         * Check shutdown in progress.
         *
         * @return TRUE if in progress
         */
        public boolean isStopping() {
            return !this.state.get().equals(ShutdownFarm.Hook.NONE);
        }

        /**
         * Complete shutdown.
         */
        public void complete() {
            this.state.set(ShutdownFarm.Hook.STOPPED);
        }

        @Override
        public String toString() {
            return this.state.get();
        }
    }
}
